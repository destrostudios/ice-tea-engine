package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.buffer.PushConstantsDataBuffer;
import com.destrostudios.icetea.core.compute.ComputeActionGroup;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.resource.descriptor.ComputeImageDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.NormalMapDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.SimpleTextureDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.util.LinkedList;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.*;

public class FftComputeJob extends ComputeJob {

    public FftComputeJob(int n, TwiddleFactorsComputeJob twiddleFactorsComputeJob, HktComputeJob hktComputeJob) {
        this.n = n;
        this.twiddleFactorsComputeJob = twiddleFactorsComputeJob;
        this.hktComputeJob = hktComputeJob;
        inversionPushConstants = new PushConstantsDataBuffer();
        dxTexture = new Texture();
        dxTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
        dxTexture.setDescriptor("default", new SimpleTextureDescriptor());
        dyTexture = new Texture();
        dyTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
        dyTexture.setDescriptor("normalMap", new NormalMapDescriptor());
        dyTexture.setDescriptor("default", new SimpleTextureDescriptor());
        dzTexture = new Texture();
        dzTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
        dzTexture.setDescriptor("default", new SimpleTextureDescriptor());
        dxPingPongTexture = new Texture();
        dxPingPongTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
        dyPingPongTexture = new Texture();
        dyPingPongTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
        dzPingPongTexture = new Texture();
        dzPingPongTexture.setDescriptor("compute", new ComputeImageDescriptor("rgba32f", false));
    }
    private int n;
    private TwiddleFactorsComputeJob twiddleFactorsComputeJob;
    private HktComputeJob hktComputeJob;
    private PushConstantsDataBuffer[] horizontalPushConstants;
    private PushConstantsDataBuffer[] verticalPushConstants;
    private PushConstantsDataBuffer inversionPushConstants;
    @Getter
    private Texture dxTexture;
    @Getter
    private Texture dyTexture;
    @Getter
    private Texture dzTexture;
    private Texture dxPingPongTexture;
    private Texture dyPingPongTexture;
    private Texture dzPingPongTexture;

    @Override
    protected void initNative() {
        try (MemoryStack stack = stackPush()) {
            initTargetTexture(dxTexture, stack);
            initTargetTexture(dyTexture, stack);
            initTargetTexture(dzTexture, stack);
            initTargetTexture(dxPingPongTexture, stack);
            initTargetTexture(dyPingPongTexture, stack);
            initTargetTexture(dzPingPongTexture, stack);
        }
        super.initNative();
    }

    private void initTargetTexture(Texture texture, MemoryStack stack) {
        texture.set(
            VK_IMAGE_ASPECT_COLOR_BIT,
            VK_FORMAT_R32G32B32A32_SFLOAT,
            VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT
        );
        texture.setWidth(n);
        texture.setHeight(n);
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
            VK_SAMPLER_ADDRESS_MODE_REPEAT,
            null,
            VK_BORDER_COLOR_INT_OPAQUE_BLACK,
            VK_SAMPLER_MIPMAP_MODE_NEAREST,
            stack
        );
        texture.updateNative(application);
    }

    @Override
    protected List<ComputeActionGroup> createComputeActionGroups() {
        LinkedList<ComputeActionGroup> computeActionGroups = new LinkedList<>();

        int stages = (int) MathUtil.log2(n);
        horizontalPushConstants = new PushConstantsDataBuffer[stages];
        verticalPushConstants = new PushConstantsDataBuffer[stages];
        int pingPongIndex = 0;
        for (int i = 0; i < stages; i++) {
            horizontalPushConstants[i] = new PushConstantsDataBuffer();
            horizontalPushConstants[i].getData().setInt("stage", i);
            horizontalPushConstants[i].getData().setInt("pingpong", pingPongIndex);
            horizontalPushConstants[i].getData().setInt("direction", 0);
            horizontalPushConstants[i].updateNative(application);

            pingPongIndex++;
            pingPongIndex %= 2;
        }
        for (int i = 0; i < stages; i++) {
            verticalPushConstants[i] = new PushConstantsDataBuffer();
            verticalPushConstants[i].getData().setInt("stage", i);
            verticalPushConstants[i].getData().setInt("pingpong", pingPongIndex);
            verticalPushConstants[i].getData().setInt("direction", 1);
            verticalPushConstants[i].updateNative(application);

            pingPongIndex++;
            pingPongIndex %= 2;
        }

        inversionPushConstants.getData().setInt("n", n);
        inversionPushConstants.getData().setInt("pingPongIndex", pingPongIndex);
        inversionPushConstants.updateNative(application);

        FftButterflyComputeActionGroup butterflyComputeActionGroup = new FftButterflyComputeActionGroup(n, horizontalPushConstants, verticalPushConstants);
        butterflyComputeActionGroup.addComputeAction(new FftButterflyComputeAction(twiddleFactorsComputeJob.getTwiddleFactorsTexture().getDescriptor("read"), hktComputeJob.getDxCoefficientsTexture().getDescriptor("read"), dxPingPongTexture.getDescriptor("compute")));
        butterflyComputeActionGroup.addComputeAction(new FftButterflyComputeAction(twiddleFactorsComputeJob.getTwiddleFactorsTexture().getDescriptor("read"), hktComputeJob.getDyCoefficientsTexture().getDescriptor("read"), dyPingPongTexture.getDescriptor("compute")));
        butterflyComputeActionGroup.addComputeAction(new FftButterflyComputeAction(twiddleFactorsComputeJob.getTwiddleFactorsTexture().getDescriptor("read"), hktComputeJob.getDzCoefficientsTexture().getDescriptor("read"), dzPingPongTexture.getDescriptor("compute")));
        computeActionGroups.add(butterflyComputeActionGroup);

        FftInversionComputeActionGroup inverseComputeActionGroup = new FftInversionComputeActionGroup(n, inversionPushConstants);
        inverseComputeActionGroup.addComputeAction(new FftInversionComputeAction(dxTexture.getDescriptor("compute"), hktComputeJob.getDxCoefficientsTexture().getDescriptor("read"), dxPingPongTexture.getDescriptor("compute")));
        inverseComputeActionGroup.addComputeAction(new FftInversionComputeAction(dyTexture.getDescriptor("compute"), hktComputeJob.getDyCoefficientsTexture().getDescriptor("read"), dyPingPongTexture.getDescriptor("compute")));
        inverseComputeActionGroup.addComputeAction(new FftInversionComputeAction(dzTexture.getDescriptor("compute"), hktComputeJob.getDzCoefficientsTexture().getDescriptor("read"), dzPingPongTexture.getDescriptor("compute")));
        computeActionGroups.add(inverseComputeActionGroup);

        return computeActionGroups;
    }

    @Override
    protected boolean shouldCreateSignalSemaphore() {
        return true;
    }

    @Override
    public void updateNative() {
        super.updateNative();
        for (PushConstantsDataBuffer horizontalPushConstantsBuffer : horizontalPushConstants) {
            horizontalPushConstantsBuffer.updateNative(application);
        }
        for (PushConstantsDataBuffer verticalPushConstantsBuffer : verticalPushConstants) {
            verticalPushConstantsBuffer.updateNative(application);
        }
        inversionPushConstants.updateNative(application);
        dxTexture.updateNative(application);
        dyTexture.updateNative(application);
        dzTexture.updateNative(application);
        dxPingPongTexture.updateNative(application);
        dyPingPongTexture.updateNative(application);
        dzPingPongTexture.updateNative(application);
    }

    @Override
    protected void cleanupNativeInternal() {
        dzPingPongTexture.cleanupNative();
        dyPingPongTexture.cleanupNative();
        dxPingPongTexture.cleanupNative();
        dzTexture.cleanupNative();
        dyTexture.cleanupNative();
        dxTexture.cleanupNative();
        for (PushConstantsDataBuffer horizontalPushConstantsBuffer : horizontalPushConstants) {
            horizontalPushConstantsBuffer.cleanupNative();
        }
        for (PushConstantsDataBuffer verticalPushConstantsBuffer : horizontalPushConstants) {
            verticalPushConstantsBuffer.cleanupNative();
        }
        super.cleanupNativeInternal();
    }
}
