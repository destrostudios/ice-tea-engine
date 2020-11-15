package com.destrostudios.icetea.core;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class Geometry extends Spatial {

    public Geometry() {
        transformUniformData = new UniformData();
    }
    @Getter
    private Mesh mesh;
    @Getter
    private Material material;
    private Long descriptorPool;
    @Getter
    private Long descriptorSetLayout;
    @Getter
    private GraphicsPipeline graphicsPipeline;
    @Getter
    private UniformData transformUniformData;
    @Getter
    private List<Long> descriptorSets;

    @Override
    public boolean update(Application application) {
        boolean commandBufferOutdated = super.update(application);
        if (transformUniformData.recreateBufferIfNecessary() | material.getParameters().recreateBufferIfNecessary()) {
            cleanupDescriptorSetLayout();
            initDescriptorSetLayout();
            cleanupDescriptorDependencies();
            createDescriptorDependencies();
            commandBufferOutdated = true;
        }
        return commandBufferOutdated;
    }

    @Override
    public void init() {
        super.init();
        if (!mesh.isInitialized()) {
            mesh.init(application);
        }
        transformUniformData.setApplication(application);
        material.getParameters().setApplication(application);
    }

    @Override
    protected void updateWorldTransform() {
        super.updateWorldTransform();
        updateWorldTransformUniform();
    }

    private void updateWorldTransformUniform() {
        transformUniformData.setMatrix4f("model", worldTransform.getMatrix());
    }

    private void initDescriptorSetLayout() {
        try (MemoryStack stack = stackPush()) {
            int descriptorsCount = getDescriptorsCount();
            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.callocStack(descriptorsCount, stack);

            int descriptorIndex = 0;

            VkDescriptorSetLayoutBinding cameraTransformDescriptorLayoutBinding = bindings.get(descriptorIndex);
            cameraTransformDescriptorLayoutBinding.binding(descriptorIndex);
            cameraTransformDescriptorLayoutBinding.descriptorCount(1);
            cameraTransformDescriptorLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraTransformDescriptorLayoutBinding.pImmutableSamplers(null);
            cameraTransformDescriptorLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            descriptorIndex++;

            VkDescriptorSetLayoutBinding transformDescriptorLayoutBinding = bindings.get(descriptorIndex);
            transformDescriptorLayoutBinding.binding(descriptorIndex);
            transformDescriptorLayoutBinding.descriptorCount(1);
            transformDescriptorLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            transformDescriptorLayoutBinding.pImmutableSamplers(null);
            transformDescriptorLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);
            descriptorIndex++;

            if (material.getParameters().getSize() > 0) {
                VkDescriptorSetLayoutBinding materialParametersLayoutBinding = bindings.get(descriptorIndex);
                materialParametersLayoutBinding.binding(descriptorIndex);
                materialParametersLayoutBinding.descriptorCount(1);
                materialParametersLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                materialParametersLayoutBinding.pImmutableSamplers(null);
                materialParametersLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
                descriptorIndex++;
            }

            for (int i = descriptorIndex; i < descriptorsCount; i++) {
                VkDescriptorSetLayoutBinding imageSamplerLayoutBinding = bindings.get(i);
                imageSamplerLayoutBinding.binding(i);
                imageSamplerLayoutBinding.descriptorCount(1);
                imageSamplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                imageSamplerLayoutBinding.pImmutableSamplers(null);
                imageSamplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
            }

            VkDescriptorSetLayoutCreateInfo layoutCreateInfo = VkDescriptorSetLayoutCreateInfo.callocStack(stack);
            layoutCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutCreateInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            if (vkCreateDescriptorSetLayout(application.getLogicalDevice(), layoutCreateInfo, null, pDescriptorSetLayout) != VK_SUCCESS) {
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

    public void createDescriptorDependencies() {
        graphicsPipeline = new GraphicsPipeline(application, this);
        initDescriptorPool();
        initDescriptorSets();
    }

    public void cleanupDescriptorDependencies() {
        if (graphicsPipeline != null) {
            graphicsPipeline.cleanup();
            graphicsPipeline = null;
            cleanupDescriptorSets();
            cleanupDescriptorPool();
        }
    }

    private void initDescriptorPool() {
        try (MemoryStack stack = stackPush()) {
            int descriptorSetsCount = getDescriptorSetsCount();
            int descriptorsCount = getDescriptorsCount();
            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.callocStack(descriptorsCount, stack);

            int descriptorIndex = 0;

            VkDescriptorPoolSize cameraTransformDescriptorsPoolSize = poolSizes.get(descriptorIndex);
            cameraTransformDescriptorsPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraTransformDescriptorsPoolSize.descriptorCount(descriptorSetsCount);
            descriptorIndex++;

            VkDescriptorPoolSize geometryTransformDescriptorsPoolSize = poolSizes.get(descriptorIndex);
            geometryTransformDescriptorsPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            geometryTransformDescriptorsPoolSize.descriptorCount(descriptorSetsCount);
            descriptorIndex++;

            if (material.getParameters().getSize() > 0) {
                VkDescriptorPoolSize materialParametersPoolSize = poolSizes.get(descriptorIndex);
                materialParametersPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                materialParametersPoolSize.descriptorCount(descriptorSetsCount);
                descriptorIndex++;
            }

            for (int i = descriptorIndex; i < descriptorsCount; i++) {
                VkDescriptorPoolSize textureSamplerPoolSize = poolSizes.get(i);
                textureSamplerPoolSize.type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
                textureSamplerPoolSize.descriptorCount(descriptorSetsCount);
            }

            VkDescriptorPoolCreateInfo poolCreateInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
            poolCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            poolCreateInfo.pPoolSizes(poolSizes);
            poolCreateInfo.maxSets(descriptorSetsCount);

            LongBuffer pDescriptorPool = stack.mallocLong(1);
            if (vkCreateDescriptorPool(application.getLogicalDevice(), poolCreateInfo, null, pDescriptorPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create descriptor pool");
            }
            descriptorPool = pDescriptorPool.get(0);
        }
    }

    private void cleanupDescriptorPool() {
        if (descriptorPool != null) {
            vkDestroyDescriptorPool(application.getLogicalDevice(), descriptorPool, null);
            descriptorPool = null;
        }
    }

    private void initDescriptorSets() {
        try (MemoryStack stack = stackPush()) {
            VkDescriptorSetAllocateInfo allocateInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
            allocateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocateInfo.descriptorPool(descriptorPool);

            int descriptorSetsCount = getDescriptorSetsCount();
            LongBuffer layouts = stack.mallocLong(descriptorSetsCount);
            for (int i = 0; i < layouts.capacity(); i++) {
                layouts.put(i, descriptorSetLayout);
            }
            allocateInfo.pSetLayouts(layouts);

            LongBuffer pDescriptorSets = stack.mallocLong(descriptorSetsCount);
            if (vkAllocateDescriptorSets(application.getLogicalDevice(), allocateInfo, pDescriptorSets) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate descriptor sets");
            }
            descriptorSets = new ArrayList<>(pDescriptorSets.capacity());

            int descriptorsCount = getDescriptorsCount();
            VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(descriptorsCount, stack);

            int descriptorIndex = 0;

            VkWriteDescriptorSet cameraTransformDescriptorWrite = descriptorWrites.get(descriptorIndex);
            cameraTransformDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            cameraTransformDescriptorWrite.dstBinding(descriptorIndex);
            cameraTransformDescriptorWrite.dstArrayElement(0);
            cameraTransformDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraTransformDescriptorWrite.descriptorCount(1);
            VkDescriptorBufferInfo.Buffer cameraTransformDescriptorBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
            cameraTransformDescriptorBufferInfo.offset(0);
            cameraTransformDescriptorBufferInfo.range(application.getCamera().getTransformUniformData().getSize());
            cameraTransformDescriptorWrite.pBufferInfo(cameraTransformDescriptorBufferInfo);
            descriptorIndex++;

            VkWriteDescriptorSet geometryTransformDescriptorWrite = descriptorWrites.get(descriptorIndex);
            geometryTransformDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            geometryTransformDescriptorWrite.dstBinding(descriptorIndex);
            geometryTransformDescriptorWrite.dstArrayElement(0);
            geometryTransformDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            geometryTransformDescriptorWrite.descriptorCount(1);
            VkDescriptorBufferInfo.Buffer geometryTransformDescriptorBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
            geometryTransformDescriptorBufferInfo.offset(0);
            geometryTransformDescriptorBufferInfo.range(transformUniformData.getSize());
            geometryTransformDescriptorWrite.pBufferInfo(geometryTransformDescriptorBufferInfo);
            descriptorIndex++;

            VkDescriptorBufferInfo.Buffer materialParameterDescriptorBufferInfo = null;
            if (material.getParameters().getSize() > 0) {
                VkWriteDescriptorSet materialParameterDescriptorWrite = descriptorWrites.get(descriptorIndex);
                materialParameterDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                materialParameterDescriptorWrite.dstBinding(descriptorIndex);
                materialParameterDescriptorWrite.dstArrayElement(0);
                materialParameterDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                materialParameterDescriptorWrite.descriptorCount(1);
                materialParameterDescriptorBufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
                materialParameterDescriptorBufferInfo.offset(0);
                materialParameterDescriptorBufferInfo.range(material.getParameters().getSize());
                materialParameterDescriptorWrite.pBufferInfo(materialParameterDescriptorBufferInfo);
                descriptorIndex++;
            }

            for (int i = descriptorIndex; i < descriptorsCount; i++) {
                Texture texture = material.getTextures().get(i - descriptorIndex);

                VkWriteDescriptorSet imageDescriptorWrite = descriptorWrites.get(i);
                imageDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
                imageDescriptorWrite.dstBinding(i);
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
                for (int r = 0; r < descriptorsCount; r++) {
                    VkWriteDescriptorSet descriptorWrite = descriptorWrites.get(r);
                    descriptorWrite.dstSet(descriptorSet);
                }

                cameraTransformDescriptorBufferInfo.buffer(application.getCamera().getTransformUniformData().getUniformBuffers().get(i));
                if (geometryTransformDescriptorBufferInfo != null) {
                    geometryTransformDescriptorBufferInfo.buffer(transformUniformData.getUniformBuffers().get(i));
                }
                if (materialParameterDescriptorBufferInfo != null) {
                    materialParameterDescriptorBufferInfo.buffer(material.getParameters().getUniformBuffers().get(i));
                }

                vkUpdateDescriptorSets(application.getLogicalDevice(), descriptorWrites, null);
                descriptorSets.add(descriptorSet);
            }
        }
    }

    private void cleanupDescriptorSets() {
        if (descriptorSets != null) {
            for (long descriptorSet : descriptorSets) {
                vkFreeDescriptorSets(application.getLogicalDevice(), descriptorPool, descriptorSet);
            }
            descriptorSets = null;
        }
    }

    private int getDescriptorSetsCount() {
        return application.getSwapChain().getImages().size();
    }

    private int getDescriptorsCount() {
        int descriptorsCount = 2;
        if (material.getParameters().getSize() > 0) {
            descriptorsCount++;
        }
        descriptorsCount += material.getTextures().size();
        return descriptorsCount;
    }

    public void setMesh(Mesh mesh) {
        tryUnregisterMesh();
        this.mesh = mesh;
        mesh.increaseUsingGeometriesCount();
    }

    public void setMaterial(Material material) {
        tryUnregisterMaterial();
        this.material = material;
        material.increaseUsingGeometriesCount();
    }

    public void updateUniformBuffers(int currentImage, MemoryStack stack) {
        transformUniformData.updateBufferIfNecessary(currentImage, stack);
        material.getParameters().updateBufferIfNecessary(currentImage, stack);
    }

    public void cleanup() {
        tryUnregisterMesh();
        tryUnregisterMaterial();
        cleanupDescriptorSetLayout();
        cleanupDescriptorDependencies();
    }

    private void tryUnregisterMesh() {
        if (mesh != null) {
            mesh.decreaseUsingGeometriesCount();
            if (mesh.isUnused()) {
                mesh.cleanup();
            }
        }
    }

    private void tryUnregisterMaterial() {
        if (material != null) {
            material.decreaseUsingGeometriesCount();
            if (material.isUnused()) {
                material.cleanup();
            }
        }
    }
}
