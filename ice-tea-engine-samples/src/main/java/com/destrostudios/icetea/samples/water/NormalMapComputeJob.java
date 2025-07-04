package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.resource.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

public class NormalMapComputeJob extends ComputeJob {

    public NormalMapComputeJob(WaterConfig waterConfig, FftComputeJob fftComputeJob) {
        this.waterConfig = waterConfig;
        this.fftComputeJob = fftComputeJob;
        pushConstants = new PushConstantsDataBuffer();
        normalMapTexture = new Texture();
        normalMapTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", true));
        normalMapTexture.setDescriptor("default", new SimpleTextureDescriptor());
    }
    private WaterConfig waterConfig;
    private FftComputeJob fftComputeJob;
    private PushConstantsDataBuffer pushConstants;
    @Getter
    private Texture normalMapTexture;

    @Override
    protected void initNative() {
        initNormalMapTexture();
        normalMapTexture.updateNative(application);
        super.initNative();
    }

    private void initNormalMapTexture() {
        try (MemoryStack stack = stackPush()) {
            int width = waterConfig.getN();
            int height = waterConfig.getN();
            int log2n = getLog2N();
            int mipLevels = log2n;
            int maxLod = log2n;
            int format = VK_FORMAT_R32G32B32A32_SFLOAT;

            LongBuffer pImage = stack.mallocLong(1);
            PointerBuffer pImageAllocation = stack.mallocPointer(1);
            application.getImageManager().createImage(
                width,
                height,
                mipLevels,
                VK_SAMPLE_COUNT_1_BIT,
                format,
                VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE,
                1,
                pImage,
                pImageAllocation
            );
            long image = pImage.get(0);
            long imageAllocation = pImageAllocation.get(0);

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
            samplerCreateInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerCreateInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerCreateInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerCreateInfo.maxAnisotropy(1);
            samplerCreateInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerCreateInfo.minLod(0); // Optional
            samplerCreateInfo.maxLod(maxLod);
            samplerCreateInfo.mipLodBias(0); // Optional
            samplerCreateInfo.unnormalizedCoordinates(false);
            samplerCreateInfo.compareEnable(false);
            samplerCreateInfo.compareOp(VK_COMPARE_OP_ALWAYS);

            LongBuffer pImageSampler = stack.mallocLong(1);
            int result = vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image sampler (result = " + result + ")");
            }
            long imageSampler = pImageSampler.get(0);

            normalMapTexture.set(image, imageAllocation, imageView, finalLayout, imageSampler);
        }
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        pushConstants.getData().setInt("n", waterConfig.getN());
        pushConstants.getData().setFloat("strength", waterConfig.getNormalStrength());
        pushConstants.updateNative(application);

        NormalMapComputeActionGroup normalMapComputeActionGroup = new NormalMapComputeActionGroup(waterConfig.getN(), pushConstants);
        normalMapComputeActionGroup.addComputeAction(new NormalMapComputeAction(normalMapTexture.getDescriptor("compute"), fftComputeJob.getDyTexture().getDescriptor("normalMap")));
        computeActionGroups.add(normalMapComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    public void updateNative() {
        super.updateNative();
        pushConstants.updateNative(application);
        normalMapTexture.updateNative(application);
    }

    @Override
    public void submit() {
        int width = waterConfig.getN();
        int height = waterConfig.getN();
        int mipLevels = getLog2N();
        application.getImageManager().transitionImageLayout(normalMapTexture.getImage(), VK_FORMAT_R32G32B32A32_SFLOAT, VK_IMAGE_LAYOUT_GENERAL, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, mipLevels);
        application.getImageManager().generateMipmaps(
            normalMapTexture.getImage(),
            VK_FORMAT_R32G32B32A32_SFLOAT,
            width,
            height,
            mipLevels,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            VK_IMAGE_LAYOUT_GENERAL,
            VK_ACCESS_SHADER_READ_BIT,
            VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT
        );
        super.submit();
    }

    private int getLog2N() {
        return (int) MathUtil.log2(waterConfig.getN());
    }

    @Override
    protected void cleanupNativeInternal() {
        normalMapTexture.cleanupNative();
        super.cleanupNativeInternal();
    }
}
