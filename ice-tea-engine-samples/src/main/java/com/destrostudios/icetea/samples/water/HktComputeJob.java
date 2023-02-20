package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.resource.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class HktComputeJob extends ComputeJob {

    public HktComputeJob(WaterConfig waterConfig, H0kComputeJob h0kComputeJob) {
        this.waterConfig = waterConfig;
        this.h0kComputeJob = h0kComputeJob;
        dxCoefficientsTexture = new Texture();
        dxCoefficientsTexture.setDescriptor("write", new ComputeImageDescriptor("rgba32f", true));
        dxCoefficientsTexture.setDescriptor("read", new ComputeImageDescriptor("rgba32f", false));

        dyCoefficientsTexture = new Texture();
        dyCoefficientsTexture.setDescriptor("write", new ComputeImageDescriptor("rgba32f", true));
        dyCoefficientsTexture.setDescriptor("read", new ComputeImageDescriptor("rgba32f", false));

        dzCoefficientsTexture = new Texture();
        dzCoefficientsTexture.setDescriptor("write", new ComputeImageDescriptor("rgba32f", true));
        dzCoefficientsTexture.setDescriptor("read", new ComputeImageDescriptor("rgba32f", false));
    }
    private WaterConfig waterConfig;
    private H0kComputeJob h0kComputeJob;
    @Getter
    private Texture dxCoefficientsTexture;
    @Getter
    private Texture dyCoefficientsTexture;
    @Getter
    private Texture dzCoefficientsTexture;
    private UniformDataBuffer uniformBuffer;
    @Setter
    private float time;

    @Override
    protected void init() {
        initTargetTexture(dxCoefficientsTexture);
        dxCoefficientsTexture.update(application, 0);

        initTargetTexture(dyCoefficientsTexture);
        dyCoefficientsTexture.update(application, 0);

        initTargetTexture(dzCoefficientsTexture);
        dzCoefficientsTexture.update(application, 0);

        initUniformBuffer();
        super.init();
    }

    private void initTargetTexture(Texture texture) {
        try (MemoryStack stack = stackPush()) {
            int width = waterConfig.getN();
            int height = waterConfig.getN();
            int format = VK_FORMAT_R32G32B32A32_SFLOAT;
            int mipLevels = 1;

            LongBuffer pImage = stack.mallocLong(1);
            LongBuffer pImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                width,
                height,
                mipLevels,
                VK_SAMPLE_COUNT_1_BIT,
                format,
                VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                1,
                pImage,
                pImageMemory
            );
            long image = pImage.get(0);
            long imageMemory = pImageMemory.get(0);

            int finalLayout = VK_IMAGE_LAYOUT_GENERAL;
            application.getImageManager().transitionImageLayout(image, format, VK_IMAGE_LAYOUT_UNDEFINED, finalLayout, mipLevels);

            long imageView = application.getImageManager().createImageView(
                image,
                VK_FORMAT_R32G32B32A32_SFLOAT,
                VK_IMAGE_ASPECT_COLOR_BIT,
                mipLevels
            );

            VkSamplerCreateInfo samplerCreateInfo = VkSamplerCreateInfo.callocStack(stack);
            samplerCreateInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerCreateInfo.magFilter(VK_FILTER_LINEAR);
            samplerCreateInfo.minFilter(VK_FILTER_LINEAR);
            samplerCreateInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE);
            samplerCreateInfo.maxAnisotropy(1);
            samplerCreateInfo.borderColor(VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE);
            samplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerCreateInfo.minLod(0); // Optional
            samplerCreateInfo.maxLod(1);
            samplerCreateInfo.mipLodBias(0); // Optional

            LongBuffer pImageSampler = stack.mallocLong(1);
            int result = vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image sampler (result = " + result + ")");
            }
            long imageSampler = pImageSampler.get(0);

            texture.set(image, imageMemory, imageView, finalLayout, imageSampler);
        }
    }

    private void initUniformBuffer() {
        uniformBuffer = new UniformDataBuffer();
        uniformBuffer.getData().setInt("N", waterConfig.getN());
        uniformBuffer.getData().setInt("L", waterConfig.getL());
        uniformBuffer.getData().setFloat("t", 0f);
        uniformBuffer.setDescriptor("default", new UniformDescriptor(VK_SHADER_STAGE_COMPUTE_BIT));
        uniformBuffer.update(application, 0);
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        HktComputeActionGroup hktComputeActionGroup = new HktComputeActionGroup(waterConfig.getN());
        hktComputeActionGroup.addComputeAction(new HktComputeAction(dxCoefficientsTexture.getDescriptor("write"), dyCoefficientsTexture.getDescriptor("write"), dzCoefficientsTexture.getDescriptor("write"), h0kComputeJob.getH0kTexture().getDescriptor("read"), h0kComputeJob.getH0minuskTexture().getDescriptor("read"), uniformBuffer.getDescriptor("default")));
        computeActionGroups.add(hktComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    protected boolean shouldCreateSignalSemaphore() {
        return true;
    }

    @Override
    protected void prepareResourcesUpdate() {
        super.prepareResourcesUpdate();
        setResourceActive(dxCoefficientsTexture);
        setResourceActive(dyCoefficientsTexture);
        setResourceActive(dzCoefficientsTexture);
        uniformBuffer.getData().setFloat("t", time);
        setResourceActive(uniformBuffer);
    }

    @Override
    protected void cleanupInternal() {
        uniformBuffer.cleanup();
        dzCoefficientsTexture.cleanup();
        dyCoefficientsTexture.cleanup();
        dxCoefficientsTexture.cleanup();
        super.cleanupInternal();
    }
}
