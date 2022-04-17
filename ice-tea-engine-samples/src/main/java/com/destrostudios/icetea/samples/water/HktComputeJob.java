package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.data.UniformData;
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
    }
    private WaterConfig waterConfig;
    private H0kComputeJob h0kComputeJob;
    @Getter
    private Texture dxCoefficientsTexture;
    @Getter
    private Texture dyCoefficientsTexture;
    @Getter
    private Texture dzCoefficientsTexture;
    @Getter
    private UniformData uniformData;
    @Setter
    private float time;

    @Override
    public void init(Application application) {
        this.application = application;
        dyCoefficientsTexture = createTargetTexture();
        dxCoefficientsTexture = createTargetTexture();
        dzCoefficientsTexture = createTargetTexture();
        initUniformData();
        super.init(application);
    }

    private Texture createTargetTexture() {
        try (MemoryStack stack = stackPush()) {
            int width = waterConfig.getN();
            int height = waterConfig.getN();

            LongBuffer pImage = stack.mallocLong(1);
            LongBuffer pImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                width,
                height,
                1,
                VK_SAMPLE_COUNT_1_BIT,
                VK_FORMAT_R32G32B32A32_SFLOAT,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                pImage,
                pImageMemory
            );
            long image = pImage.get(0);
            long imageMemory = pImageMemory.get(0);

            long imageView = application.getImageManager().createImageView(
                image,
                VK_FORMAT_R32G32B32A32_SFLOAT,
                VK_IMAGE_ASPECT_COLOR_BIT,
                1
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

            int finalLayout = VK_IMAGE_LAYOUT_GENERAL;
            Texture texture = new Texture(image, imageMemory, imageView, finalLayout, imageSampler);
            texture.update(application, 0, 0);
            return texture;
        }
    }

    private void initUniformData() {
        uniformData = new UniformData();
        uniformData.setInt("N", waterConfig.getN());
        uniformData.setInt("L", waterConfig.getL());
        uniformData.setFloat("t", 0f);
        uniformData.updateBufferAndCheckRecreation(application, 0, 0, 1);
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        HktComputeActionGroup hktComputeActionGroup = new HktComputeActionGroup(waterConfig.getN());
        hktComputeActionGroup.addComputeAction(new HktComputeAction(dyCoefficientsTexture, dxCoefficientsTexture, dzCoefficientsTexture, h0kComputeJob.getH0kTexture(), h0kComputeJob.getH0minuskTexture(), uniformData));
        computeActionGroups.add(hktComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    protected boolean shouldCreateSignalSemaphore() {
        return true;
    }

    @Override
    public void update(Application application, int imageIndex, float tpf) {
        super.update(application, imageIndex, tpf);
        uniformData.setFloat("t", time);
        uniformData.updateBufferAndCheckRecreation(application, 0, tpf, 1);
    }

    @Override
    public void cleanup() {
        uniformData.cleanup();
        dzCoefficientsTexture.cleanup();
        dyCoefficientsTexture.cleanup();
        dxCoefficientsTexture.cleanup();
        super.cleanup();
    }
}
