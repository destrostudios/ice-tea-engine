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
        try (MemoryStack stack = stackPush()) {
            initTargetTexture(h0kTexture, stack);
            initTargetTexture(h0minuskTexture, stack);
        }

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

    private void initTargetTexture(Texture texture, MemoryStack stack) {
        texture.set(
            VK_IMAGE_ASPECT_COLOR_BIT,
            VK_FORMAT_R32G32B32A32_SFLOAT,
            VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT
        );
        texture.setWidth(waterConfig.getN());
        texture.setHeight(waterConfig.getN());
        texture.updateNative(application);
        texture.createImage(stack);
        application.getCommandPool().executeSingleTimeCommands(commandBuffer -> {
            texture.transitionLayout(
                commandBuffer,
                VK_IMAGE_LAYOUT_GENERAL,
                VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                0,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                stack
            );
        });
        texture.createImageView(stack);
        texture.createSampler(
            VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE,
            null,
            VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE,
            VK_SAMPLER_MIPMAP_MODE_LINEAR,
            stack
        );
        texture.updateNative(application);
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
