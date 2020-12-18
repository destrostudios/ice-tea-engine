package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.*;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class FftComputeJob extends ComputeJob {

    public FftComputeJob(int n, Texture twiddleFactorsTexture, Texture dxCoefficientsTexture, Texture dyCoefficientsTexture, Texture dzCoefficientsTexture) {
        this.n = n;
        this.twiddleFactorsTexture = twiddleFactorsTexture;
        this.dxCoefficientsTexture = dxCoefficientsTexture;
        this.dyCoefficientsTexture = dyCoefficientsTexture;
        this.dzCoefficientsTexture = dzCoefficientsTexture;
    }
    private int n;
    private Texture twiddleFactorsTexture;
    private Texture dxCoefficientsTexture;
    private Texture dyCoefficientsTexture;
    private Texture dzCoefficientsTexture;
    @Getter
    private Texture dxTexture;
    @Getter
    private Texture dyTexture;
    @Getter
    private Texture dzTexture;
    private Texture dxPingPongTexture;
    @Getter
    private Texture dyPingPongTexture;
    private Texture dzPingPongTexture;

    @Override
    public void init(Application application) {
        this.application = application;
        dxTexture = createTargetTexture();
        dyTexture = createTargetTexture();
        dzTexture = createTargetTexture();
        dxPingPongTexture = createTargetTexture();
        dyPingPongTexture = createTargetTexture();
        dzPingPongTexture = createTargetTexture();
        super.init(application);
    }

    private Texture createTargetTexture() {
        try (MemoryStack stack = stackPush()) {
            int width = n;
            int height = n;

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
            samplerCreateInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerCreateInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerCreateInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerCreateInfo.anisotropyEnable(false);
            samplerCreateInfo.maxAnisotropy(0);
            samplerCreateInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerCreateInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_NEAREST);
            samplerCreateInfo.minLod(0); // Optional
            samplerCreateInfo.maxLod(0);
            samplerCreateInfo.mipLodBias(0); // Optional
            samplerCreateInfo.unnormalizedCoordinates(false);
            samplerCreateInfo.compareEnable(false);
            samplerCreateInfo.compareOp(VK_COMPARE_OP_ALWAYS);

            LongBuffer pImageSampler = stack.mallocLong(1);
            if (vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image sampler");
            }
            long imageSampler = pImageSampler.get(0);

            Texture texture = new Texture(image, imageMemory, imageView, imageSampler);
            texture.init(application);
            return texture;
        }
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        int stages = (int) MathUtil.log2(n);
        ByteBufferData[] horizontalPushConstants = new ByteBufferData[stages];
        ByteBufferData[] verticalPushConstants = new ByteBufferData[stages];
        int pingPongIndex = 0;
        for (int i = 0; i < stages; i++) {
            horizontalPushConstants[i] = new ByteBufferData();
            horizontalPushConstants[i].setInt("stage", i);
            horizontalPushConstants[i].setInt("pingpong", pingPongIndex);
            horizontalPushConstants[i].setInt("direction", 0);
            horizontalPushConstants[i].initBuffers(1);
            horizontalPushConstants[i].updateBufferIfNecessary(0);

            pingPongIndex++;
            pingPongIndex %= 2;
        }
        for (int i = 0; i < stages; i++) {
            verticalPushConstants[i] = new ByteBufferData();
            verticalPushConstants[i].setInt("stage", i);
            verticalPushConstants[i].setInt("pingpong", pingPongIndex);
            verticalPushConstants[i].setInt("direction", 1);
            verticalPushConstants[i].initBuffers(1);
            verticalPushConstants[i].updateBufferIfNecessary(0);

            pingPongIndex++;
            pingPongIndex %= 2;
        }

        ByteBufferData inversionPushConstants = new ByteBufferData();
        inversionPushConstants.setInt("n", n);
        inversionPushConstants.setInt("pingPongIndex", pingPongIndex);
        inversionPushConstants.initBuffers(1);
        inversionPushConstants.updateBufferIfNecessary(0);

        FftButterflyComputeActionGroup butterflyComputeActionGroup = new FftButterflyComputeActionGroup(n, horizontalPushConstants, verticalPushConstants);
        butterflyComputeActionGroup.addComputeAction(new FftButterflyComputeAction(twiddleFactorsTexture, dxCoefficientsTexture, dxPingPongTexture));
        butterflyComputeActionGroup.addComputeAction(new FftButterflyComputeAction(twiddleFactorsTexture, dyCoefficientsTexture, dyPingPongTexture));
        butterflyComputeActionGroup.addComputeAction(new FftButterflyComputeAction(twiddleFactorsTexture, dzCoefficientsTexture, dzPingPongTexture));
        computeActionGroups.add(butterflyComputeActionGroup);

        FftInversionComputeActionGroup inverseComputeActionGroup = new FftInversionComputeActionGroup(n, inversionPushConstants);
        inverseComputeActionGroup.addComputeAction(new FftInversionComputeAction(dxTexture, dxCoefficientsTexture, dxPingPongTexture));
        inverseComputeActionGroup.addComputeAction(new FftInversionComputeAction(dyTexture, dyCoefficientsTexture, dyPingPongTexture));
        inverseComputeActionGroup.addComputeAction(new FftInversionComputeAction(dzTexture, dzCoefficientsTexture, dzPingPongTexture));
        computeActionGroups.add(inverseComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    protected boolean shouldCreateSignalSemaphore() {
        return true;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        dxTexture.cleanup();
        dyTexture.cleanup();
        dzTexture.cleanup();
        dxCoefficientsTexture.cleanup();
        dyCoefficientsTexture.cleanup();
        dzCoefficientsTexture.cleanup();
    }
}
