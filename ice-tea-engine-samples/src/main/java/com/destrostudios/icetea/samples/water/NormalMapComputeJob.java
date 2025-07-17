package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.resource.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class NormalMapComputeJob extends ComputeJob {

    public NormalMapComputeJob(WaterConfig waterConfig, FftComputeJob fftComputeJob) {
        this.waterConfig = waterConfig;
        this.fftComputeJob = fftComputeJob;
        pushConstants = new PushConstantsDataBuffer();
        normalMapTexture = new Texture();
        normalMapTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", true));
        normalMapTexture.setDescriptor("default", new SimpleTextureDescriptor());
    }
    private WaterConfig waterConfig;
    private FftComputeJob fftComputeJob;
    private PushConstantsDataBuffer pushConstants;
    @Getter
    private Texture normalMapTexture;

    @Override
    protected void initNative() {
        initNormalMapTexture();
        super.initNative();
    }

    private void initNormalMapTexture() {
        try (MemoryStack stack = stackPush()) {
            int mipLevels = (int) MathUtil.log2(waterConfig.getN());
            normalMapTexture.set(
                VK_IMAGE_ASPECT_COLOR_BIT,
                VK_FORMAT_R32G32B32A32_SFLOAT,
                1,
                mipLevels,
                VK_SAMPLE_COUNT_1_BIT,
                // VK_IMAGE_USAGE_TRANSFER_SRC_BIT and VK_IMAGE_USAGE_TRANSFER_DST_BIT are needed for mipmap generation
                VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT
            );
            normalMapTexture.setWidth(waterConfig.getN());
            normalMapTexture.setHeight(waterConfig.getN());
            normalMapTexture.updateNative(application);
            normalMapTexture.createImage(stack);
            application.getCommandPool().executeSingleTimeCommands(commandBuffer -> {
                normalMapTexture.transitionLayout(
                    commandBuffer,
                    VK_IMAGE_LAYOUT_GENERAL,
                    VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                    0,
                    VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                    VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                    stack
                );
            });
            normalMapTexture.createImageView(stack);
            normalMapTexture.createSampler(
                VK_SAMPLER_ADDRESS_MODE_REPEAT,
                null,
                VK_BORDER_COLOR_INT_OPAQUE_BLACK,
                VK_SAMPLER_MIPMAP_MODE_LINEAR,
                stack
            );
            normalMapTexture.updateNative(application);
        }
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        pushConstants.getData().setInt("n", waterConfig.getN());
        pushConstants.getData().setFloat("strength", waterConfig.getNormalStrength());
        pushConstants.updateNative(application);

        NormalMapComputeActionGroup normalMapComputeActionGroup = new NormalMapComputeActionGroup(waterConfig.getN(), pushConstants);
        normalMapComputeActionGroup.addComputeAction(new NormalMapComputeAction(normalMapTexture.getDescriptor("compute"), fftComputeJob.getDyTexture().getDescriptor("normalMap")));
        computeActionGroups.add(normalMapComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    public void updateNative() {
        super.updateNative();
        pushConstants.updateNative(application);
        normalMapTexture.updateNative(application);
    }

    @Override
    public void submit() {
        // TODO: Shouldn't this be done after rendering into it?
        try (MemoryStack stack = stackPush()) {
            application.getCommandPool().executeSingleTimeCommands(commandBuffer -> {
                normalMapTexture.generateMipmaps(commandBuffer, stack);
            });
        }
        super.submit();
    }

    @Override
    protected void cleanupNativeInternal() {
        normalMapTexture.cleanupNative();
        super.cleanupNativeInternal();
    }
}
