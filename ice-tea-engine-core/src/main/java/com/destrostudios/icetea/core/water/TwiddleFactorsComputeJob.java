package com.destrostudios.icetea.core.water;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.data.StorageBufferData;
import com.destrostudios.icetea.core.data.UniformData;
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
    }
    private int n;
    @Getter
    private Texture twiddleFactorsTexture;
    private UniformData uniformData;

    @Override
    public void init(Application application) {
        this.application = application;
        initTwiddleFactorsTexture();
        initUniformData();
        super.init(application);
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
            if (vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pImageSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create image sampler");
            }
            long imageSampler = pImageSampler.get(0);

            twiddleFactorsTexture = new Texture(image, imageMemory, imageView, imageSampler);
            twiddleFactorsTexture.init(application);
        }
    }

    private void initUniformData() {
        uniformData = new UniformData();
        uniformData.setApplication(application);
        uniformData.setInt("n", n);
        uniformData.initBuffers(1);
        uniformData.updateBufferIfNecessary(0);
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

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        StorageBufferData storageBufferData = new StorageBufferData();
        storageBufferData.setApplication(application);
        storageBufferData.setIntArray("bitReversedIndices", getBitReversedIndices(n));
        storageBufferData.initBuffers(1);
        storageBufferData.updateBufferIfNecessary(0);

        TwiddleFactorsComputeActionGroup twiddleFactorsComputeActionGroup = new TwiddleFactorsComputeActionGroup(n);
        twiddleFactorsComputeActionGroup.addComputeAction(new TwiddleFactorsComputeAction(twiddleFactorsTexture, storageBufferData, uniformData));
        computeActionGroups.add(twiddleFactorsComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        twiddleFactorsTexture.cleanup();
        uniformData.cleanupBuffer();
    }
}
