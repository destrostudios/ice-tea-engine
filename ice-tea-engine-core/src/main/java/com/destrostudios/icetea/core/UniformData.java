package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class UniformData {

    public UniformData() {
        fields = new LinkedHashMap<>();
    }
    @Setter
    private Application application;
    private Map<String, UniformValue<?>> fields;
    @Getter
    private int size;
    @Getter
    private ArrayList<Long> uniformBuffers;
    private ArrayList<Long> uniformBuffersMemory;
    private boolean structureModified;
    private ArrayList<Boolean> contentModified;

    public void setVector3f(String name, Vector3f value) {
        set(name, value, Vector3fUniformValue::new);
    }

    public void setVector4f(String name, Vector4f value) {
        set(name, value, Vector4fUniformValue::new);
    }

    public void setMatrix4f(String name, Matrix4f value) {
        set(name, value, Matrix4fUniformValue::new);
    }

    private <T> void set(String name, T value, Supplier<UniformValue<T>> uniformDataSupplier) {
        UniformValue<T> uniformValue = (UniformValue<T>) fields.get(name);
        if (uniformValue == null) {
            uniformValue = uniformDataSupplier.get();
            fields.put(name, uniformValue);
            size += uniformValue.getSize();
            structureModified = true;
        }
        uniformValue.setValue(value);
        onContentModified();
    }

    public void clear(String name) {
        UniformValue<?> value = fields.remove(name);
        size -= value.getSize();
        structureModified = true;
        onContentModified();
    }

    private void onContentModified() {
        if (contentModified != null) {
            for (int i = 0; i < contentModified.size(); i++) {
                contentModified.set(i, true);
            }
        }
    }

    public Vector3f getVector3f(String name) {
        return get(name);
    }

    public Vector4f getVector4f(String name) {
        return get(name);
    }

    public Matrix4f getMatrix4f(String name) {
        return get(name);
    }

    private <T> T get(String name) {
        UniformValue<T> uniformValue = (UniformValue<T>) fields.get(name);
        return ((uniformValue != null) ? uniformValue.getValue() : null);
    }

    public boolean recreateBufferIfNecessary() {
        if (structureModified) {
            cleanupBuffer();
            initBuffer();
            structureModified = false;
            return true;
        }
        return false;
    }

    public void initBuffer() {
        if (size > 0) {
            try (MemoryStack stack = stackPush()) {
                int swapChainImagesCount = application.getSwapChain().getImages().size();
                uniformBuffers = new ArrayList<>(swapChainImagesCount);
                uniformBuffersMemory = new ArrayList<>(swapChainImagesCount);
                contentModified = new ArrayList<>(swapChainImagesCount);
                LongBuffer pBuffer = stack.mallocLong(1);
                LongBuffer pBufferMemory = stack.mallocLong(1);
                for (int i = 0; i < swapChainImagesCount; i++) {
                    application.getBufferManager().createBuffer(
                            size,
                            VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                            pBuffer,
                            pBufferMemory
                    );
                    uniformBuffers.add(pBuffer.get(0));
                    uniformBuffersMemory.add(pBufferMemory.get(0));
                    contentModified.add(true);
                }
            }
        }
        structureModified = false;
    }

    public void updateBufferIfNecessary(int currentImage, MemoryStack stack) {
        if ((size > 0) && contentModified.get(currentImage)) {
            long uniformBufferMemory = uniformBuffersMemory.get(currentImage);
            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(application.getLogicalDevice(), uniformBufferMemory, 0, size, 0, data);
            ByteBuffer byteBuffer = data.getByteBuffer(0, size);
            int index = 0;
            for (UniformValue<?> value : fields.values()) {
                value.write(byteBuffer, index);
                index += value.getSize();
            }
            vkUnmapMemory(application.getLogicalDevice(), uniformBufferMemory);
            contentModified.set(currentImage, false);
        }
    }

    public void cleanupBuffer() {
        if (uniformBuffers != null) {
            for (long uniformBuffer : uniformBuffers) {
                vkDestroyBuffer(application.getLogicalDevice(), uniformBuffer, null);
            }
            uniformBuffers = null;
        }
        if (uniformBuffersMemory != null) {
            for (long uniformBufferMemory : uniformBuffersMemory) {
                vkFreeMemory(application.getLogicalDevice(), uniformBufferMemory, null);
            }
            uniformBuffersMemory = null;
        }
    }
}