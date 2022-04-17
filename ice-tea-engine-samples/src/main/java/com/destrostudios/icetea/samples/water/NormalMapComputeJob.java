package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.data.ByteBufferData;
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

public class NormalMapComputeJob extends ComputeJob {

    public NormalMapComputeJob(WaterConfig waterConfig, Texture dyTexture) {
        this.waterConfig = waterConfig;
        this.dyTexture = dyTexture;
    }
    private WaterConfig waterConfig;
    private Texture dyTexture;
    @Getter
    private Texture normalMapTexture;

    @Override
    public void init(Application application) {
        this.application = application;
        normalMapTexture = createNormalMapTexture();
        super.init(application);
    }

    private Texture createNormalMapTexture() {
        try (MemoryStack stack = stackPush()) {
            int width = waterConfig.getN();
            int height = waterConfig.getN();
            int log2n = getLog2N();
            int mipLevels = log2n;
            int maxLod = log2n;

            LongBuffer pImage = stack.mallocLong(1);
            LongBuffer pImageMemory = stack.mallocLong(1);
            application.getImageManager().createImage(
                width,
                height,
                mipLevels,
                VK_SAMPLE_COUNT_1_BIT,
                VK_FORMAT_R32G32B32A32_SFLOAT,
                VK_IMAGE_TILING_OPTIMAL,
                VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
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

            int finalLayout = VK_IMAGE_LAYOUT_GENERAL;
            Texture texture = new Texture(image, imageMemory, imageView, finalLayout, imageSampler);
            texture.update(application, 0, 0);
            return texture;
        }
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        ByteBufferData pushConstants = new ByteBufferData();
        pushConstants.setInt("n", waterConfig.getN());
        pushConstants.setFloat("strength", waterConfig.getNormalStrength());
        pushConstants.updateBufferAndCheckRecreation(application, 0, 0, 1);

        NormalMapComputeActionGroup normalMapComputeActionGroup = new NormalMapComputeActionGroup(waterConfig.getN(), pushConstants);
        normalMapComputeActionGroup.addComputeAction(new NormalMapComputeAction(normalMapTexture, dyTexture));
        computeActionGroups.add(normalMapComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    public void submit() {
        super.submit();

        int width = waterConfig.getN();
        int height = waterConfig.getN();
        int mipLevels = getLog2N();
        application.getImageManager().generateMipmaps(
            normalMapTexture.getImage(),
            VK_FORMAT_R32G32B32A32_SFLOAT,
            width,
            height,
            mipLevels,
            VK_IMAGE_LAYOUT_GENERAL,
            VK_ACCESS_SHADER_READ_BIT,
            VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT
        );
    }

    private int getLog2N() {
        return (int) MathUtil.log2(waterConfig.getN());
    }

    @Override
    public void cleanup() {
        super.cleanup();
        normalMapTexture.cleanup();
    }
}
