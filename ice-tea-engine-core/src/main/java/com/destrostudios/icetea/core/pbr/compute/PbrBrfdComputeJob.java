package com.destrostudios.icetea.core.pbr.compute;

import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.pbr.PbrConfig;
import com.destrostudios.icetea.core.resource.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.UniformDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class PbrBrfdComputeJob extends ComputeJob {

    public PbrBrfdComputeJob(PbrConfig pbrConfig) {
        this.pbrConfig = pbrConfig;
        brdfLookupTexture = new Texture();
        brdfLookupTexture.setDescriptor("write", new ComputeImageDescriptor("rgba32f", true));
        brdfLookupTexture.setDescriptor("read", new ComputeImageDescriptor("rgba32f", false));
        brdfLookupTexture.setDescriptor("default", new SimpleTextureDescriptor());
    }
    private PbrConfig pbrConfig;
    @Getter
    private Texture brdfLookupTexture;
    private UniformDataBuffer uniformBuffer;

    @Override
    protected void initNative() {
        initBrfdLookupTexture();
        initUniformBuffer();
        super.initNative();
    }

    private void initBrfdLookupTexture() {
        try (MemoryStack stack = stackPush()) {
            brdfLookupTexture.set(
                VK_IMAGE_ASPECT_COLOR_BIT,
                VK_FORMAT_R16G16_SFLOAT,
                VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT
            );
            brdfLookupTexture.setWidth(pbrConfig.getBrfdLookupTextureSize());
            brdfLookupTexture.setHeight(pbrConfig.getBrfdLookupTextureSize());
            brdfLookupTexture.updateNative(application);
            brdfLookupTexture.createImage(stack);
            application.getCommandPool().executeSingleTimeCommands(commandBuffer -> {
                brdfLookupTexture.transitionLayout(
                    commandBuffer,
                    VK_IMAGE_LAYOUT_GENERAL,
                    VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                    0,
                    VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                    VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                    stack
                );
            });
            brdfLookupTexture.createImageView(stack);
            brdfLookupTexture.createSampler(
                VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE,
                null,
                VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE,
                VK_SAMPLER_MIPMAP_MODE_LINEAR,
                stack
            );
            brdfLookupTexture.updateNative(application);
        }
    }

    private void initUniformBuffer() {
        uniformBuffer = new UniformDataBuffer();
        uniformBuffer.getData().setInt("textureSize", pbrConfig.getBrfdLookupTextureSize());
        uniformBuffer.getData().setInt("numSamples", pbrConfig.getBrfdLookupTextureSamples());
        uniformBuffer.setDescriptor("default", new UniformDescriptor(VK_SHADER_STAGE_COMPUTE_BIT));
        uniformBuffer.updateNative(application);
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        PbrBrfdComputeActionGroup pbrBrfdComputeActionGroup = new PbrBrfdComputeActionGroup(pbrConfig.getBrfdLookupTextureSize());
        pbrBrfdComputeActionGroup.addComputeAction(new PbrBrfdComputeAction(brdfLookupTexture.getDescriptor("write"), uniformBuffer.getDescriptor("default")));
        computeActionGroups.add(pbrBrfdComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    public void updateNative() {
        super.updateNative();
        brdfLookupTexture.updateNative(application);
        uniformBuffer.updateNative(application);
    }

    @Override
    protected void cleanupNativeInternal() {
        uniformBuffer.cleanupNative();
        // Don't cleanup the texture which was passed to the created PbrEnvironment (which owns and controls its lifetime)
        super.cleanupNativeInternal();
    }
}
