package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.asset.loader.BufferedTextureLoaderSettings;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.resource.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.UniformDescriptor;
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

        h0kTexture = new Texture();
        h0kTexture.setDescriptor("write", new ComputeImageDescriptor("rgba32f", true));
        h0kTexture.setDescriptor("read", new ComputeImageDescriptor("rgba32f", false));

        h0minuskTexture = new Texture();
        h0minuskTexture.setDescriptor("write", new ComputeImageDescriptor("rgba32f", true));
        h0minuskTexture.setDescriptor("read", new ComputeImageDescriptor("rgba32f", false));
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
    private UniformDataBuffer uniformBuffer;

    @Override
    protected void initNative() {
        initTargetTexture(h0kTexture);
        h0kTexture.updateNative(application);

        initTargetTexture(h0minuskTexture);
        h0minuskTexture.updateNative(application);

        BufferedTextureLoaderSettings noiseTextureSettings = BufferedTextureLoaderSettings.builder()
                .format(VK_FORMAT_R8G8B8A8_UNORM)
                .usage(VK_IMAGE_USAGE_STORAGE_BIT)
                .layout(VK_IMAGE_LAYOUT_GENERAL)
                .createDefaultDescriptor(false)
                .build();

        noiseTexture1 = application.getAssetManager().loadTexture("com/destrostudios/icetea/samples/textures/water/noise_" + waterConfig.getN() + "_0.jpg", noiseTextureSettings);
        noiseTexture1.setDescriptor("compute", new ComputeImageDescriptor("rgba8", false));
        noiseTexture1.updateNative(application);

        noiseTexture2 = application.getAssetManager().loadTexture("com/destrostudios/icetea/samples/textures/water/noise_" + waterConfig.getN() + "_1.jpg", noiseTextureSettings);
        noiseTexture2.setDescriptor("compute", new ComputeImageDescriptor("rgba8", false));
        noiseTexture2.updateNative(application);

        noiseTexture3 = application.getAssetManager().loadTexture("com/destrostudios/icetea/samples/textures/water/noise_" + waterConfig.getN() + "_2.jpg", noiseTextureSettings);
        noiseTexture3.setDescriptor("compute", new ComputeImageDescriptor("rgba8", false));
        noiseTexture3.updateNative(application);

        noiseTexture4 = application.getAssetManager().loadTexture("com/destrostudios/icetea/samples/textures/water/noise_" + waterConfig.getN() + "_3.jpg", noiseTextureSettings);
        noiseTexture4.setDescriptor("compute", new ComputeImageDescriptor("rgba8", false));
        noiseTexture4.updateNative(application);

        initUniformBuffer();
        super.initNative();
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

            texture.set(image, imageMemory, imageView, finalLayout, imageSampler);
        }
    }

    private void initUniformBuffer() {
        uniformBuffer = new UniformDataBuffer();
        uniformBuffer.getData().setInt("N", waterConfig.getN());
        uniformBuffer.getData().setInt("L", waterConfig.getL());
        uniformBuffer.getData().setFloat("amplitude", waterConfig.getAmplitude());
        uniformBuffer.getData().setFloat("windspeed", waterConfig.getWindSpeed());
        uniformBuffer.getData().setVector2f("w", waterConfig.getWindDirection());
        uniformBuffer.getData().setFloat("capillarSupressFactor", waterConfig.getCapillarSuppressFactor());
        uniformBuffer.setDescriptor("default", new UniformDescriptor(VK_SHADER_STAGE_COMPUTE_BIT));
        uniformBuffer.updateNative(application);
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        H0kComputeActionGroup h0kComputeActionGroup = new H0kComputeActionGroup(waterConfig.getN());
        h0kComputeActionGroup.addComputeAction(new H0kComputeAction(h0kTexture.getDescriptor("write"), h0minuskTexture.getDescriptor("write"), noiseTexture1.getDescriptor("compute"), noiseTexture2.getDescriptor("compute"), noiseTexture3.getDescriptor("compute"), noiseTexture4.getDescriptor("compute"), uniformBuffer.getDescriptor("default")));
        computeActionGroups.add(h0kComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    public void updateNative() {
        super.updateNative();
        h0kTexture.updateNative(application);
        h0minuskTexture.updateNative(application);
        noiseTexture1.updateNative(application);
        noiseTexture2.updateNative(application);
        noiseTexture3.updateNative(application);
        noiseTexture4.updateNative(application);
        uniformBuffer.updateNative(application);
    }

    @Override
    protected void cleanupNativeInternal() {
        uniformBuffer.cleanupNative();
        noiseTexture4.cleanupNative();
        noiseTexture3.cleanupNative();
        noiseTexture2.cleanupNative();
        noiseTexture1.cleanupNative();
        h0minuskTexture.cleanupNative();
        h0kTexture.cleanupNative();
        super.cleanupNativeInternal();
    }
}
