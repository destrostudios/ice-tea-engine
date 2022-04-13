package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkSamplerCreateInfo;

import java.nio.LongBuffer;
import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class H0kComputeJob extends ComputeJob {

    public H0kComputeJob(WaterConfig waterConfig) {
        this.waterConfig = waterConfig;
    }
    private WaterConfig waterConfig;
    @Getter
    private Texture h0kTexture;
    @Getter
    private Texture h0minuskTexture;
    private Texture noiseTexture1;
    private Texture noiseTexture2;
    private Texture noiseTexture3;
    private Texture noiseTexture4;
    private UniformData uniformData;

    @Override
    public void init(Application application) {
        this.application = application;
        h0kTexture = createTargetTexture();
        h0minuskTexture = createTargetTexture();
        noiseTexture1 = application.getAssetManager().loadTexture("com/destrostudios/icetea/samples/textures/water/noise_" + waterConfig.getN() + "_0.jpg");
        noiseTexture1.init(application);
        noiseTexture2 = application.getAssetManager().loadTexture("com/destrostudios/icetea/samples/textures/water/noise_" + waterConfig.getN() + "_1.jpg");
        noiseTexture2.init(application);
        noiseTexture3 = application.getAssetManager().loadTexture("com/destrostudios/icetea/samples/textures/water/noise_" + waterConfig.getN() + "_2.jpg");
        noiseTexture3.init(application);
        noiseTexture4 = application.getAssetManager().loadTexture("com/destrostudios/icetea/samples/textures/water/noise_" + waterConfig.getN() + "_3.jpg");
        noiseTexture4.init(application);
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
            texture.init(application);
            return texture;
        }
    }

    private void initUniformData() {
        uniformData = new UniformData();
        uniformData.setApplication(application);
        uniformData.setInt("N", waterConfig.getN());
        uniformData.setInt("L", waterConfig.getL());
        uniformData.setFloat("amplitude", waterConfig.getAmplitude());
        uniformData.setFloat("windspeed", waterConfig.getWindSpeed());
        uniformData.setVector2f("w", waterConfig.getWindDirection());
        uniformData.setFloat("capillarSupressFactor", waterConfig.getCapillarSuppressFactor());
        uniformData.initBuffers(1);
        uniformData.updateBufferIfNecessary(0);
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        H0kComputeActionGroup h0kComputeActionGroup = new H0kComputeActionGroup(waterConfig.getN());
        h0kComputeActionGroup.addComputeAction(new H0kComputeAction(h0kTexture, h0minuskTexture, noiseTexture1, noiseTexture2, noiseTexture3, noiseTexture4, uniformData));
        computeActionGroups.add(h0kComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        h0kTexture.cleanup();
        h0minuskTexture.cleanup();
        noiseTexture1.cleanup();
        noiseTexture2.cleanup();
        noiseTexture3.cleanup();
        noiseTexture4.cleanup();
        uniformData.cleanupBuffer();
    }
}
