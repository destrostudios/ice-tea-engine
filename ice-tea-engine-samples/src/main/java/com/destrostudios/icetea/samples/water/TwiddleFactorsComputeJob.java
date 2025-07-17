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
    protected void initNative() {
        initTwiddleFactorsTexture();
        initStorageBuffer();
        initUniformBuffer();
        super.initNative();
    }

    private void initTwiddleFactorsTexture() {
        try (MemoryStack stack = stackPush()) {
            twiddleFactorsTexture.set(
                VK_IMAGE_ASPECT_COLOR_BIT,
                VK_FORMAT_R32G32B32A32_SFLOAT,
                VK_IMAGE_USAGE_STORAGE_BIT | VK_IMAGE_USAGE_SAMPLED_BIT
            );
            twiddleFactorsTexture.setWidth((int) MathUtil.log2(n));
            twiddleFactorsTexture.setHeight(n);
            twiddleFactorsTexture.updateNative(application);
            twiddleFactorsTexture.createImage(stack);
            application.getCommandPool().executeSingleTimeCommands(commandBuffer -> {
                twiddleFactorsTexture.transitionLayout(
                    commandBuffer,
                    VK_IMAGE_LAYOUT_GENERAL,
                    VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT,
                    0,
                    VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                    VK_ACCESS_SHADER_READ_BIT | VK_ACCESS_SHADER_WRITE_BIT,
                    stack
                );
            });
            twiddleFactorsTexture.createImageView(stack);
            twiddleFactorsTexture.createSampler(
                VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_EDGE,
                null,
                VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE,
                VK_SAMPLER_MIPMAP_MODE_LINEAR,
                stack
            );
            twiddleFactorsTexture.updateNative(application);
        }
    }

    private void initStorageBuffer() {
        storageBuffer = new StorageDataBuffer();
        storageBuffer.getData().setIntArray("bitReversedIndices", getBitReversedIndices(n));
        storageBuffer.setDescriptor("default", new StorageBufferDescriptor(VK_SHADER_STAGE_COMPUTE_BIT));
        storageBuffer.updateNative(application);
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
        uniformBuffer.updateNative(application);
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
    public void updateNative() {
        super.updateNative();
        twiddleFactorsTexture.updateNative(application);
        storageBuffer.updateNative(application);
        uniformBuffer.updateNative(application);
    }

    @Override
    protected void cleanupNativeInternal() {
        uniformBuffer.cleanupNative();
        storageBuffer.cleanupNative();
        twiddleFactorsTexture.cleanupNative();
        super.cleanupNativeInternal();
    }
}
