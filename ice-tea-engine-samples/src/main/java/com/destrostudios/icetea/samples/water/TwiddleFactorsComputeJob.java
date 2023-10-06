package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.buffer.StorageDataBuffer;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.resource.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.StorageBufferDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

public class TwiddleFactorsComputeJob extends ComputeJob {

    public TwiddleFactorsComputeJob(int n) {
        this.n = n;
        twiddleFactorsTexture = new Texture();
        twiddleFactorsTexture.setDescriptor("write", new ComputeImageDescriptor("rgba32f", true));
        twiddleFactorsTexture.setDescriptor("read", new ComputeImageDescriptor("rgba32f", false));
    }
    private int n;
    @Getter
    private Texture twiddleFactorsTexture;
    private StorageDataBuffer storageBuffer;
    private UniformDataBuffer uniformBuffer;

    @Override
    protected void initNative() {
        initTwiddleFactorsTexture();
        initStorageBuffer();
        initUniformBuffer();
        super.initNative();
    }

    private void initTwiddleFactorsTexture() {
        try (MemoryStack stack = stackPush()) {
            int width = (int) MathUtil.log2(n);
            int height = n;
            int format = VK_FORMAT_R32G32B32A32_SFLOAT;
            int mipLevels = 1;

            LongBuffer pImage = stack.mallocLong(1);
            PointerBuffer pImageAllocation = stack.mallocPointer(1);
            application.getImageManager().createImage(
                width,
                height,
                mipLevels,
                VK_SAMPLE_COUNT_1_BIT,
                format,
                VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
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

            twiddleFactorsTexture.set(image, imageAllocation, imageView, finalLayout, imageSampler);
            twiddleFactorsTexture.updateNative(application);
        }
    }

    private void initStorageBuffer() {
        storageBuffer = new StorageDataBuffer();
        storageBuffer.getData().setIntArray("bitReversedIndices", getBitReversedIndices(n));
        storageBuffer.setDescriptor("default", new StorageBufferDescriptor(VK_SHADER_STAGE_COMPUTE_BIT));
        storageBuffer.updateNative(application);
    }

    public static int[] getBitReversedIndices(int n) {
        int[] bitReversedIndices = new int[n];
        int bits = (int) (Math.log(n) / Math.log(2));
        for (int i = 0; i < n; i++)  {
            int x = Integer.reverse(i);
            x = Integer.rotateLeft(x, bits);
            bitReversedIndices[i] = x;
        }
        return bitReversedIndices;
    }

    private void initUniformBuffer() {
        uniformBuffer = new UniformDataBuffer();
        uniformBuffer.getData().setInt("n", n);
        uniformBuffer.setDescriptor("default", new UniformDescriptor(VK_SHADER_STAGE_COMPUTE_BIT));
        uniformBuffer.updateNative(application);
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        TwiddleFactorsComputeActionGroup twiddleFactorsComputeActionGroup = new TwiddleFactorsComputeActionGroup(n);
        twiddleFactorsComputeActionGroup.addComputeAction(new TwiddleFactorsComputeAction(twiddleFactorsTexture.getDescriptor("write"), storageBuffer.getDescriptor("default"), uniformBuffer.getDescriptor("default")));
        computeActionGroups.add(twiddleFactorsComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    public void updateNative() {
        super.updateNative();
        twiddleFactorsTexture.updateNative(application);
        storageBuffer.updateNative(application);
        uniformBuffer.updateNative(application);
    }

    @Override
    protected void cleanupNativeInternal() {
        uniformBuffer.cleanupNative();
        storageBuffer.cleanupNative();
        twiddleFactorsTexture.cleanupNative();
        super.cleanupNativeInternal();
    }
}
