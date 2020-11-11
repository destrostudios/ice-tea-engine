package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Geometry {

    public Geometry(Mesh mesh) {
        this.mesh = mesh;
    }
    private Application application;
    @Getter
    private Mesh mesh;
    @Getter
    @Setter
    private Material material;
    @Getter
    private Transform localTransform = new Transform();
    @Getter
    private Transform worldTransform = new Transform();
    private boolean isWorldTransformOutdated;
    @Getter
    private Long descriptorSetLayout;
    @Getter
    private GraphicsPipeline graphicsPipeline;
    @Getter
    private List<Long> uniformBuffers;
    @Getter
    private List<Long> uniformBuffersMemory;
    @Getter
    private List<Long> descriptorSets;

    public void init(Application application) {
        this.application = application;
        mesh.init(application);
        recreateDescriptorSetLayout();
    }

    private void recreateDescriptorSetLayout() {
        cleanupDescriptorSetLayout();

        try (MemoryStack stack = stackPush()) {
            int bindingsCount = 1 + material.getTextures().size();

            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(bindingsCount, stack);

            VkDescriptorSetLayoutBinding uniformBufferLayoutBinding = bindings.get(0);
            uniformBufferLayoutBinding.binding(0);
            uniformBufferLayoutBinding.descriptorCount(1);
            uniformBufferLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uniformBufferLayoutBinding.pImmutableSamplers(null);
            uniformBufferLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

            for (int i = 1; i < bindingsCount; i++) {
                VkDescriptorSetLayoutBinding imageSamplerLayoutBinding = bindings.get(i);
                imageSamplerLayoutBinding.binding(1);
                imageSamplerLayoutBinding.descriptorCount(1);
                imageSamplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                imageSamplerLayoutBinding.pImmutableSamplers(null);
                imageSamplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
            }

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

    public void initGraphicsPipeline() {
        graphicsPipeline = new GraphicsPipeline(application, this);
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
                    UniformBuffer.SIZEOF,
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

            int bindingsCount = 1 + material.getTextures().size();
            VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(bindingsCount, stack);

            VkWriteDescriptorSet uniformBufferDescriptorWrite = descriptorWrites.get(0);
            uniformBufferDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uniformBufferDescriptorWrite.dstBinding(0);
            uniformBufferDescriptorWrite.dstArrayElement(0);
            uniformBufferDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uniformBufferDescriptorWrite.descriptorCount(1);
            VkDescriptorBufferInfo.Buffer uniformBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
            uniformBufferInfo.offset(0);
            uniformBufferInfo.range(UniformBuffer.SIZEOF);
            uniformBufferDescriptorWrite.pBufferInfo(uniformBufferInfo);

            for (int i = 1; i < bindingsCount; i++) {
                Texture texture = material.getTextures().get(i - 1);

                VkWriteDescriptorSet imageDescriptorWrite = descriptorWrites.get(i);
                imageDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                imageDescriptorWrite.dstBinding(1);
                imageDescriptorWrite.dstArrayElement(0);
                imageDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                imageDescriptorWrite.descriptorCount(1);
                VkDescriptorImageInfo.Buffer imageBufferInfo = VkDescriptorImageInfo.callocStack(1, stack);
                imageBufferInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                imageBufferInfo.imageView(texture.getImageView());
                imageBufferInfo.sampler(texture.getSampler());
                imageDescriptorWrite.pImageInfo(imageBufferInfo);
            }

            for (int i = 0; i < pDescriptorSets.capacity(); i++) {
                long descriptorSet = pDescriptorSets.get(i);
                for (int r = 0; r < bindingsCount; r++) {
                    VkWriteDescriptorSet descriptorWrite = descriptorWrites.get(r);
                    descriptorWrite.dstSet(descriptorSet);
                }
                uniformBufferInfo.buffer(uniformBuffers.get(i));

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
        material.cleanup();
        mesh.cleanup();
        cleanupDescriptorSetLayout();
        cleanupSwapChainDependencies();
    }

    public void cleanupSwapChainDependencies() {
        cleanupUniformBuffers();
        cleanupDescriptorSets();
    }

    public void setLocalTranslation(Vector3fc translation) {
        localTransform.setTranslation(translation);
    }

    public void setLocalRotation(Quaternionfc rotation) {
        localTransform.setRotation(rotation);
    }

    public void setLocalScale(Vector3fc scale) {
        localTransform.setScale(scale);
    }

    public void move(Vector3fc translation) {
        localTransform.move(translation);
    }

    public void rotate(Quaternionfc rotation) {
        localTransform.rotate(rotation);
    }

    public void scale(Vector3fc scale) {
        localTransform.scale(scale);
    }

    public void update() {
        if (localTransform.updateMatrixIfNecessary()) {
            isWorldTransformOutdated = true;
        }
        if (isWorldTransformOutdated) {
            worldTransform.set(localTransform);
            isWorldTransformOutdated = false;
        }
    }
}

