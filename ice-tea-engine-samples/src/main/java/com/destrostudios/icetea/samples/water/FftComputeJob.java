package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.resource.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.NormalMapDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class FftComputeJob extends ComputeJob {

    public FftComputeJob(int n, TwiddleFactorsComputeJob twiddleFactorsComputeJob, HktComputeJob hktComputeJob) {
        this.n = n;
        this.twiddleFactorsComputeJob = twiddleFactorsComputeJob;
        this.hktComputeJob = hktComputeJob;
        inversionPushConstants = new PushConstantsDataBuffer();
        dxTexture = new Texture();
        dxTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
        dxTexture.setDescriptor("default", new SimpleTextureDescriptor());
        dyTexture = new Texture();
        dyTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
        dyTexture.setDescriptor("normalMap", new NormalMapDescriptor());
        dyTexture.setDescriptor("default", new SimpleTextureDescriptor());
        dzTexture = new Texture();
        dzTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
        dzTexture.setDescriptor("default", new SimpleTextureDescriptor());
        dxPingPongTexture = new Texture();
        dxPingPongTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
        dyPingPongTexture = new Texture();
        dyPingPongTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
        dzPingPongTexture = new Texture();
        dzPingPongTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
    }
    private int n;
    private TwiddleFactorsComputeJob twiddleFactorsComputeJob;
    private HktComputeJob hktComputeJob;
    private PushConstantsDataBuffer[] horizontalPushConstants;
    private PushConstantsDataBuffer[] verticalPushConstants;
    private PushConstantsDataBuffer inversionPushConstants;
    @Getter
    private Texture dxTexture;
    @Getter
    private Texture dyTexture;
    @Getter
    private Texture dzTexture;
    private Texture dxPingPongTexture;
    private Texture dyPingPongTexture;
    private Texture dzPingPongTexture;

    @Override
    protected void initNative() {
        initTargetTexture(dxTexture);
        dxTexture.updateNative(application);

        initTargetTexture(dyTexture);
        dyTexture.updateNative(application);

        initTargetTexture(dzTexture);
        dzTexture.updateNative(application);

        initTargetTexture(dxPingPongTexture);
        dxPingPongTexture.updateNative(application);

        initTargetTexture(dyPingPongTexture);
        dyPingPongTexture.updateNative(application);

        initTargetTexture(dzPingPongTexture);
        dzPingPongTexture.updateNative(application);

        super.initNative();
    }

    private void initTargetTexture(Texture texture) {
        try (MemoryStack stack = stackPush()) {
            int width = n;
            int height = n;
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
            int result = vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler);
            if (result != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image sampler (result = " + result + ")");
            }
            long imageSampler = pImageSampler.get(0);

            texture.set(image, imageMemory, imageView, finalLayout, imageSampler);
        }
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        int stages = (int) MathUtil.log2(n);
        horizontalPushConstants = new PushConstantsDataBuffer[stages];
        verticalPushConstants = new PushConstantsDataBuffer[stages];
        int pingPongIndex = 0;
        for (int i = 0; i < stages; i++) {
            horizontalPushConstants[i] = new PushConstantsDataBuffer();
            horizontalPushConstants[i].getData().setInt("stage", i);
            horizontalPushConstants[i].getData().setInt("pingpong", pingPongIndex);
            horizontalPushConstants[i].getData().setInt("direction", 0);
            horizontalPushConstants[i].updateNative(application);

            pingPongIndex++;
            pingPongIndex %= 2;
        }
        for (int i = 0; i < stages; i++) {
            verticalPushConstants[i] = new PushConstantsDataBuffer();
            verticalPushConstants[i].getData().setInt("stage", i);
            verticalPushConstants[i].getData().setInt("pingpong", pingPongIndex);
            verticalPushConstants[i].getData().setInt("direction", 1);
            verticalPushConstants[i].updateNative(application);

            pingPongIndex++;
            pingPongIndex %= 2;
        }

        inversionPushConstants.getData().setInt("n", n);
        inversionPushConstants.getData().setInt("pingPongIndex", pingPongIndex);
        inversionPushConstants.updateNative(application);

        FftButterflyComputeActionGroup butterflyComputeActionGroup = new FftButterflyComputeActionGroup(n, horizontalPushConstants, verticalPushConstants);
        butterflyComputeActionGroup.addComputeAction(new FftButterflyComputeAction(twiddleFactorsComputeJob.getTwiddleFactorsTexture().getDescriptor("read"), hktComputeJob.getDxCoefficientsTexture().getDescriptor("read"), dxPingPongTexture.getDescriptor("compute")));
        butterflyComputeActionGroup.addComputeAction(new FftButterflyComputeAction(twiddleFactorsComputeJob.getTwiddleFactorsTexture().getDescriptor("read"), hktComputeJob.getDyCoefficientsTexture().getDescriptor("read"), dyPingPongTexture.getDescriptor("compute")));
        butterflyComputeActionGroup.addComputeAction(new FftButterflyComputeAction(twiddleFactorsComputeJob.getTwiddleFactorsTexture().getDescriptor("read"), hktComputeJob.getDzCoefficientsTexture().getDescriptor("read"), dzPingPongTexture.getDescriptor("compute")));
        computeActionGroups.add(butterflyComputeActionGroup);

        FftInversionComputeActionGroup inverseComputeActionGroup = new FftInversionComputeActionGroup(n, inversionPushConstants);
        inverseComputeActionGroup.addComputeAction(new FftInversionComputeAction(dxTexture.getDescriptor("compute"), hktComputeJob.getDxCoefficientsTexture().getDescriptor("read"), dxPingPongTexture.getDescriptor("compute")));
        inverseComputeActionGroup.addComputeAction(new FftInversionComputeAction(dyTexture.getDescriptor("compute"), hktComputeJob.getDyCoefficientsTexture().getDescriptor("read"), dyPingPongTexture.getDescriptor("compute")));
        inverseComputeActionGroup.addComputeAction(new FftInversionComputeAction(dzTexture.getDescriptor("compute"), hktComputeJob.getDzCoefficientsTexture().getDescriptor("read"), dzPingPongTexture.getDescriptor("compute")));
        computeActionGroups.add(inverseComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    protected boolean shouldCreateSignalSemaphore() {
        return true;
    }

    @Override
    protected void prepareResourcesUpdate() {
        super.prepareResourcesUpdate();
        for (PushConstantsDataBuffer horizontalPushConstantsBuffer : horizontalPushConstants) {
            setResourceActive(horizontalPushConstantsBuffer);
        }
        for (PushConstantsDataBuffer verticalPushConstantsBuffer : verticalPushConstants) {
            setResourceActive(verticalPushConstantsBuffer);
        }
        setResourceActive(inversionPushConstants);
        setResourceActive(dxTexture);
        setResourceActive(dyTexture);
        setResourceActive(dzTexture);
        setResourceActive(dxPingPongTexture);
        setResourceActive(dyPingPongTexture);
        setResourceActive(dzPingPongTexture);
    }

    @Override
    protected void cleanupNativeInternal() {
        dzPingPongTexture.cleanupNative();
        dyPingPongTexture.cleanupNative();
        dxPingPongTexture.cleanupNative();
        dzTexture.cleanupNative();
        dyTexture.cleanupNative();
        dxTexture.cleanupNative();
        for (PushConstantsDataBuffer horizontalPushConstantsBuffer : horizontalPushConstants) {
            horizontalPushConstantsBuffer.cleanupNative();
        }
        for (PushConstantsDataBuffer verticalPushConstantsBuffer : horizontalPushConstants) {
            verticalPushConstantsBuffer.cleanupNative();
        }
        super.cleanupNativeInternal();
    }
}
