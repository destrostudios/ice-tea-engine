package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.File;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.ClassLoader.getSystemClassLoader;
import static org.lwjgl.assimp.Assimp.aiProcess_DropNormals;
import static org.lwjgl.assimp.Assimp.aiProcess_FlipUVs;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Geometry {

    private Application application;
    @Getter
    @Setter
    private Material material;
    private Vertex[] vertices;
    @Getter
    private int[] indices;
    @Getter
    private Long vertexBuffer;
    @Getter
    private Long vertexBufferMemory;
    @Getter
    private Long indexBuffer;
    @Getter
    private Long indexBufferMemory;
    @Getter
    private List<Long> uniformBuffers;
    @Getter
    private List<Long> uniformBuffersMemory;
    @Getter
    private Long descriptorSetLayout;
    @Getter
    private List<Long> descriptorSets;

    public void loadModel(String filePath) {
        File modelFile = new File(getSystemClassLoader().getResource(filePath).getFile());
        Model model = ModelLoader.loadModel(modelFile, aiProcess_FlipUVs | aiProcess_DropNormals);

        int vertexCount = model.getPositions().size();
        vertices = new Vertex[vertexCount];
        Vector3fc color = new Vector3f(1.0f, 1.0f, 1.0f);
        for (int i = 0; i < vertexCount; i++) {
            vertices[i] = new Vertex(
                model.getPositions().get(i),
                color,
                model.getTexCoords().get(i)
            );
        }

        indices = new int[model.getIndices().size()];
        for (int i = 0; i < indices.length; i++) {
            indices[i] = model.getIndices().get(i);
        }
    }

    public void init(Application application) {
        this.application = application;
        recreateVertexBuffer();
        recreateIndexBuffer();
        recreateDescriptorSetLayout();
    }

    private void recreateVertexBuffer() {
        cleanupVertexBuffer();

        try (MemoryStack stack = stackPush()) {
            long bufferSize = Vertex.SIZEOF * vertices.length;

            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);
            application.getBufferManager().createBuffer(
                    bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pBuffer,
                    pBufferMemory
            );

            long stagingBuffer = pBuffer.get(0);
            long stagingBufferMemory = pBufferMemory.get(0);

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(application.getLogicalDevice(), stagingBufferMemory, 0, bufferSize, 0, data);
            BufferUtil.memcpy(data.getByteBuffer(0, (int) bufferSize), vertices);
            vkUnmapMemory(application.getLogicalDevice(), stagingBufferMemory);

            application.getBufferManager().createBuffer(
                    bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory
            );

            vertexBuffer = pBuffer.get(0);
            vertexBufferMemory = pBufferMemory.get(0);

            application.getBufferManager().copyBuffer(stagingBuffer, vertexBuffer, bufferSize);
            vkDestroyBuffer(application.getLogicalDevice(), stagingBuffer, null);
            vkFreeMemory(application.getLogicalDevice(), stagingBufferMemory, null);
        }
    }

    private void cleanupVertexBuffer() {
        if (vertexBuffer != null) {
            vkDestroyBuffer(application.getLogicalDevice(), vertexBuffer, null);
            vertexBuffer = null;
        }
        if (vertexBufferMemory != null) {
            vkFreeMemory(application.getLogicalDevice(), vertexBufferMemory, null);
            vertexBufferMemory = null;
        }
    }

    private void recreateIndexBuffer() {
        cleanupIndexBuffer();

        try (MemoryStack stack = stackPush()) {
            long bufferSize = Integer.BYTES * indices.length;
            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);
            application.getBufferManager().createBuffer(
                bufferSize,
                VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                pBuffer,
                pBufferMemory
            );

            long stagingBuffer = pBuffer.get(0);
            long stagingBufferMemory = pBufferMemory.get(0);

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(application.getLogicalDevice(), stagingBufferMemory, 0, bufferSize, 0, data);
            BufferUtil.memcpy(data.getByteBuffer(0, (int) bufferSize), indices);
            vkUnmapMemory(application.getLogicalDevice(), stagingBufferMemory);

            application.getBufferManager().createBuffer(
                bufferSize,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                VK_MEMORY_HEAP_DEVICE_LOCAL_BIT,
                pBuffer,
                pBufferMemory
            );

            indexBuffer = pBuffer.get(0);
            indexBufferMemory = pBufferMemory.get(0);

            application.getBufferManager().copyBuffer(stagingBuffer, indexBuffer, bufferSize);
            vkDestroyBuffer(application.getLogicalDevice(), stagingBuffer, null);
            vkFreeMemory(application.getLogicalDevice(), stagingBufferMemory, null);
        }
    }

    private void cleanupIndexBuffer() {
        if (indexBuffer != null) {
            vkDestroyBuffer(application.getLogicalDevice(), indexBuffer, null);
            indexBuffer = null;
        }
        if (indexBufferMemory != null) {
            vkFreeMemory(application.getLogicalDevice(), indexBufferMemory, null);
            indexBufferMemory = null;
        }
    }

    private void recreateDescriptorSetLayout() {
        cleanupDescriptorSetLayout();

        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(2, stack);

            VkDescriptorSetLayoutBinding uboLayoutBinding = bindings.get(0);
            uboLayoutBinding.binding(0);
            uboLayoutBinding.descriptorCount(1);
            uboLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboLayoutBinding.pImmutableSamplers(null);
            uboLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

            VkDescriptorSetLayoutBinding samplerLayoutBinding = bindings.get(1);
            samplerLayoutBinding.binding(1);
            samplerLayoutBinding.descriptorCount(1);
            samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            samplerLayoutBinding.pImmutableSamplers(null);
            samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            if (vkCreateDescriptorSetLayout(application.getLogicalDevice(), layoutInfo, null, pDescriptorSetLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create descriptor set layout");
            }
            descriptorSetLayout = pDescriptorSetLayout.get(0);
        }
    }

    private void cleanupDescriptorSetLayout() {
        if (descriptorSetLayout != null) {
            vkDestroyDescriptorSetLayout(application.getLogicalDevice(), descriptorSetLayout, null);
            descriptorSetLayout = null;
        }
    }

    public void onSwapChainCreation() {
        initUniformBuffers();
        initDescriptorSets();
    }

    private void initUniformBuffers() {
        try (MemoryStack stack = stackPush()) {
            int uniformBuffersCount = application.getSwapChain().getImages().size();
            uniformBuffers = new ArrayList<>(uniformBuffersCount);
            uniformBuffersMemory = new ArrayList<>(uniformBuffersCount);
            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);
            for (int i = 0; i < uniformBuffersCount; i++) {
                application.getBufferManager().createBuffer(
                    UniformBufferObject.SIZEOF,
                    VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pBuffer,
                    pBufferMemory
                );
                uniformBuffers.add(pBuffer.get(0));
                uniformBuffersMemory.add(pBufferMemory.get(0));
            }
        }
    }

    private void cleanupUniformBuffers() {
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

    private void initDescriptorSets() {
        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
            allocateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocateInfo.descriptorPool(application.getSwapChain().getDescriptorPool());

            List<Long> swapChainImages = application.getSwapChain().getImages();
            LongBuffer layouts = stack.mallocLong(swapChainImages.size());
            for (int i = 0; i < layouts.capacity(); i++) {
                layouts.put(i, descriptorSetLayout);
            }
            allocateInfo.pSetLayouts(layouts);

            LongBuffer pDescriptorSets = stack.mallocLong(swapChainImages.size());
            if (vkAllocateDescriptorSets(application.getLogicalDevice(), allocateInfo, pDescriptorSets) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate descriptor sets");
            }
            descriptorSets = new ArrayList<>(pDescriptorSets.capacity());

            VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
            bufferInfo.offset(0);
            bufferInfo.range(UniformBufferObject.SIZEOF);

            VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
            imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            imageInfo.imageView(material.getTexture().getImageView());
            imageInfo.sampler(material.getTexture().getSampler());

            VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(2, stack);

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(1);
            uboDescriptorWrite.pBufferInfo(bufferInfo);

            VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(1);
            samplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            samplerDescriptorWrite.dstBinding(1);
            samplerDescriptorWrite.dstArrayElement(0);
            samplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            samplerDescriptorWrite.descriptorCount(1);
            samplerDescriptorWrite.pImageInfo(imageInfo);

            for (int i = 0; i < pDescriptorSets.capacity(); i++) {
                long descriptorSet = pDescriptorSets.get(i);
                uboDescriptorWrite.dstSet(descriptorSet);
                samplerDescriptorWrite.dstSet(descriptorSet);

                bufferInfo.buffer(uniformBuffers.get(i));

                vkUpdateDescriptorSets(application.getLogicalDevice(), descriptorWrites, null);
                descriptorSets.add(descriptorSet);
            }
        }
    }

    private void cleanupDescriptorSets() {
        if (descriptorSets != null) {
            for (long descriptorSet : descriptorSets) {
                vkFreeDescriptorSets(application.getLogicalDevice(), application.getSwapChain().getDescriptorPool(), descriptorSet);
            }
            descriptorSets = null;
        }
    }

    public void cleanup() {
        cleanupVertexBuffer();
        cleanupIndexBuffer();
        cleanupDescriptorSetLayout();
        cleanupSwapChainDependencies();
    }

    public void cleanupSwapChainDependencies() {
        cleanupUniformBuffers();
        cleanupDescriptorSets();
    }
}

