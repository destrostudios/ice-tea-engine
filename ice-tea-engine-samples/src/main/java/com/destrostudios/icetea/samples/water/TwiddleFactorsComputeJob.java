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
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
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
    protected void init() {
        initTwiddleFactorsTexture();
        initStorageBuffer();
        initUniformBuffer();
        super.init();
    }

    private void initTwiddleFactorsTexture() {
        try (MemoryStack stack = stackPush()) {
            int width = (int) MathUtil.log2(n);
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
            twiddleFactorsTexture.set(image, imageMemory, imageView, finalLayout, imageSampler);
            twiddleFactorsTexture.update(application, 0);
        }
    }

    private void initStorageBuffer() {
        storageBuffer = new StorageDataBuffer();
        storageBuffer.getData().setIntArray("bitReversedIndices", getBitReversedIndices(n));
        storageBuffer.setDescriptor("default", new StorageBufferDescriptor(VK_SHADER_STAGE_COMPUTE_BIT));
        storageBuffer.update(application, 0);
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
        uniformBuffer.update(application, 0);
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
    protected void cleanupInternal() {
        uniformBuffer.cleanup();
        storageBuffer.cleanup();
        twiddleFactorsTexture.cleanup();
        super.cleanupInternal();
    }
}
