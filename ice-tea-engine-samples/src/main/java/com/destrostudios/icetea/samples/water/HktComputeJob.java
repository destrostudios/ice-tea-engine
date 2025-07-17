package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.resource.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.system.MemoryStack;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class HktComputeJob extends ComputeJob {

    public HktComputeJob(WaterConfig waterConfig, H0kComputeJob h0kComputeJob) {
        this.waterConfig = waterConfig;
        this.h0kComputeJob = h0kComputeJob;

        dxCoefficientsTexture = new Texture();
        dxCoefficientsTexture.setDescriptor("write", new ComputeImageDescriptor("rgba32f", true));
        dxCoefficientsTexture.setDescriptor("read", new ComputeImageDescriptor("rgba32f", false));

        dyCoefficientsTexture = new Texture();
        dyCoefficientsTexture.setDescriptor("write", new ComputeImageDescriptor("rgba32f", true));
        dyCoefficientsTexture.setDescriptor("read", new ComputeImageDescriptor("rgba32f", false));

        dzCoefficientsTexture = new Texture();
        dzCoefficientsTexture.setDescriptor("write", new ComputeImageDescriptor("rgba32f", true));
        dzCoefficientsTexture.setDescriptor("read", new ComputeImageDescriptor("rgba32f", false));
    }
    private WaterConfig waterConfig;
    private H0kComputeJob h0kComputeJob;
    @Getter
    private Texture dxCoefficientsTexture;
    @Getter
    private Texture dyCoefficientsTexture;
    @Getter
    private Texture dzCoefficientsTexture;
    private UniformDataBuffer uniformBuffer;
    @Setter
    private float time;

    @Override
    protected void initNative() {
        try (MemoryStack stack = stackPush()) {
            initTargetTexture(dxCoefficientsTexture, stack);
            initTargetTexture(dyCoefficientsTexture, stack);
            initTargetTexture(dzCoefficientsTexture, stack);
        }
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
        uniformBuffer.getData().setFloat("t", 0f);
        uniformBuffer.setDescriptor("default", new UniformDescriptor(VK_SHADER_STAGE_COMPUTE_BIT));
        uniformBuffer.updateNative(application);
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        HktComputeActionGroup hktComputeActionGroup = new HktComputeActionGroup(waterConfig.getN());
        hktComputeActionGroup.addComputeAction(new HktComputeAction(dxCoefficientsTexture.getDescriptor("write"), dyCoefficientsTexture.getDescriptor("write"), dzCoefficientsTexture.getDescriptor("write"), h0kComputeJob.getH0kTexture().getDescriptor("read"), h0kComputeJob.getH0minuskTexture().getDescriptor("read"), uniformBuffer.getDescriptor("default")));
        computeActionGroups.add(hktComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    protected boolean shouldCreateSignalSemaphore() {
        return true;
    }

    @Override
    public void updateNative() {
        super.updateNative();
        dxCoefficientsTexture.updateNative(application);
        dyCoefficientsTexture.updateNative(application);
        dzCoefficientsTexture.updateNative(application);
        uniformBuffer.getData().setFloat("t", time);
        uniformBuffer.updateNative(application);
    }

    @Override
    protected void cleanupNativeInternal() {
        uniformBuffer.cleanupNative();
        dzCoefficientsTexture.cleanupNative();
        dyCoefficientsTexture.cleanupNative();
        dxCoefficientsTexture.cleanupNative();
        super.cleanupNativeInternal();
    }
}
