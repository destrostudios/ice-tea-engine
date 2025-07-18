package com.destrostudios.icetea.core.texture;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.resource.Resource;
import lombok.Getter;
import lombok.Setter;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocationCreateInfo;
import org.lwjgl.util.vma.VmaAllocationInfo;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;

import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

public class Texture extends Resource {

    @Getter
    protected int aspectMask;
    @Getter
    protected int format;
    @Getter
    protected int mipLevels;
    @Getter
    protected int layers;
    @Getter
    protected int samples;
    @Getter
    protected int usage;
    @Getter
    @Setter
    protected int width;
    @Getter
    @Setter
    protected int height;
    @Getter
    protected Long image;
    @Getter
    protected Long imageAllocation;
    @Getter
    protected Long imageView;
    @Getter
    protected Long sampler;
    protected int[][] layouts;
    protected int[][] automaticallyTransitionedLayouts;
    private boolean wasJustCreated;

    public void set(int aspectMask, int format, int usage) {
        set(aspectMask, format, 1, 1, VK_SAMPLE_COUNT_1_BIT, usage);
    }

    public void set(int aspectMask, int format, int layers, int mipLevels, int samples, int usage) {
        this.aspectMask = aspectMask;
        this.format = format;
        this.layers = layers;
        this.mipLevels = mipLevels;
        this.samples = samples;
        this.usage = usage;
        layouts = new int[layers][mipLevels];
    }

    public void createImage(MemoryStack stack) {
        createImage(0, stack);
    }

    public void createImage(int flags, MemoryStack stack) {
        VkImageCreateInfo imageCreateInfo = VkImageCreateInfo.callocStack(stack);
        imageCreateInfo.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
        imageCreateInfo.imageType(VK_IMAGE_TYPE_2D);
        imageCreateInfo.extent().width(width);
        imageCreateInfo.extent().height(height);
        imageCreateInfo.extent().depth(1);
        imageCreateInfo.mipLevels(mipLevels);
        imageCreateInfo.arrayLayers(layers);
        imageCreateInfo.format(format);
        imageCreateInfo.tiling(VK_IMAGE_TILING_OPTIMAL);
        imageCreateInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        imageCreateInfo.usage(usage);
        imageCreateInfo.samples(samples);
        imageCreateInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);
        imageCreateInfo.flags(flags);

        VmaAllocationCreateInfo allocationCreateInfo = VmaAllocationCreateInfo.callocStack(stack);
        allocationCreateInfo.usage(VMA_MEMORY_USAGE_AUTO_PREFER_DEVICE);

        VmaAllocationInfo allocationInfo = VmaAllocationInfo.callocStack(stack);

        LongBuffer pImage = stack.mallocLong(1);
        PointerBuffer pImageAllocation = stack.mallocPointer(1);
        int result = vmaCreateImage(application.getMemoryManager().getAllocator(), imageCreateInfo, allocationCreateInfo, pImage, pImageAllocation, allocationInfo);
        if (result != VK_SUCCESS) {
            throw new RuntimeException("Failed to create image (result = " + result + ")");
        }
        image = pImage.get(0);
        imageAllocation = pImageAllocation.get(0);

        wasJustCreated = true;
    }

    @Override
    protected void updateResource() {
        if (wasJustCreated) {
            setOutdated();
            wasJustCreated = false;
        }
    }

    public void copyFromBuffer(VkCommandBuffer commandBuffer, long buffer, MemoryStack stack) {
        copyFromBuffer(commandBuffer, buffer, 0, stack);
    }

    public void copyFromBuffer(VkCommandBuffer commandBuffer, long buffer, int mipLevel, MemoryStack stack) {
        int tmpLayout = getLayout(mipLevel);

        transitionLayout(
            commandBuffer,
            VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
            0,
            VK_PIPELINE_STAGE_TRANSFER_BIT,
            VK_ACCESS_TRANSFER_WRITE_BIT,
            mipLevel,
            stack
        );

        VkBufferImageCopy.Buffer region = VkBufferImageCopy.callocStack(1, stack);
        region.imageSubresource().aspectMask(aspectMask);
        region.imageSubresource().mipLevel(mipLevel);
        region.imageSubresource().layerCount(layers);
        region.imageExtent(VkExtent3D.callocStack(stack).set(width, height, 1));

        vkCmdCopyBufferToImage(commandBuffer, buffer, image, getLayout(mipLevel), region);

        transitionLayout(
            commandBuffer,
            tmpLayout,
            VK_PIPELINE_STAGE_TRANSFER_BIT,
            VK_ACCESS_TRANSFER_WRITE_BIT,
            VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
            0,
            mipLevel,
            stack
        );
    }

    public void copyFromTexture(VkCommandBuffer commandBuffer, Texture srcTexture, int dstArrayLayer, int dstMipLevel, int regionWidth, int regionHeight, MemoryStack stack) {
        copyFromTexture(commandBuffer, srcTexture, 0, 0, dstArrayLayer, dstMipLevel, regionWidth, regionHeight, stack);
    }

    // TODO: This methods behaviour (not doing the transitions itself for performance reasons) should probably be aligned with copyFromBuffer+generateMipmaps (which do them themselves, in a slightly unperformant way)
    public void copyFromTexture(VkCommandBuffer commandBuffer, Texture srcTexture, int srcLayer, int srcMipLevel, int dstArrayLayer, int dstMipLevel, int regionWidth, int regionHeight, MemoryStack stack) {
        VkImageCopy.Buffer region = VkImageCopy.callocStack(1, stack);

        region.srcSubresource().aspectMask(srcTexture.getAspectMask());
        region.srcSubresource().baseArrayLayer(srcLayer);
        region.srcSubresource().layerCount(1);
        region.srcSubresource().mipLevel(srcMipLevel);

        region.dstSubresource().aspectMask(aspectMask);
        region.dstSubresource().baseArrayLayer(dstArrayLayer);
        region.dstSubresource().layerCount(1);
        region.dstSubresource().mipLevel(dstMipLevel);

        region.extent().width(regionWidth);
        region.extent().height(regionHeight);
        region.extent().depth(1);

        vkCmdCopyImage(commandBuffer, srcTexture.getImage(), srcTexture.getLayout(srcLayer, srcMipLevel), image, getLayout(dstArrayLayer, dstMipLevel), region);
    }

    public void generateMipmaps(VkCommandBuffer commandBuffer, MemoryStack stack) {
        if (mipLevels <= 1) {
            return;
        }

        // Check if format supports linear blitting
        VkFormatProperties formatProperties = VkFormatProperties.mallocStack(stack);
        vkGetPhysicalDeviceFormatProperties(application.getPhysicalDevice(), format, formatProperties);
        if ((formatProperties.optimalTilingFeatures() & VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT) == 0) {
            throw new RuntimeException("Texture format does not support linear blitting");
        }

        int[] tmpLayouts = new int[mipLevels];
        int mipWidth = width;
        int mipHeight = height;
        for (int mipLevel = 0; mipLevel < mipLevels; mipLevel++) {
            tmpLayouts[mipLevel] = getLayout(mipLevel);
            if (mipLevel == 0) {
                continue;
            }

            transitionLayout(
                commandBuffer,
                VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                (mipLevel == 1) ? VK_PIPELINE_STAGE_ALL_COMMANDS_BIT : VK_PIPELINE_STAGE_TRANSFER_BIT,
                (mipLevel == 1) ? 0 : VK_ACCESS_TRANSFER_WRITE_BIT,
                VK_PIPELINE_STAGE_TRANSFER_BIT,
                VK_ACCESS_TRANSFER_READ_BIT,
                mipLevel - 1,
                stack
            );

            transitionLayout(
                commandBuffer,
                VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                0,
                VK_PIPELINE_STAGE_TRANSFER_BIT,
                VK_ACCESS_TRANSFER_WRITE_BIT,
                mipLevel,
                stack
            );

            int nextMipWidth = (mipWidth > 1) ? (mipWidth / 2) : 1;
            int nextMipHeight = (mipHeight > 1) ? (mipHeight / 2) : 1;

            VkImageBlit.Buffer blit = VkImageBlit.callocStack(1, stack);
            blit.srcOffsets(0).set(0, 0, 0);
            blit.srcOffsets(1).set(mipWidth, mipHeight, 1);
            blit.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            blit.srcSubresource().baseArrayLayer(0);
            blit.srcSubresource().layerCount(1);
            blit.srcSubresource().mipLevel(mipLevel - 1);
            blit.dstOffsets(0).set(0, 0, 0);
            blit.dstOffsets(1).set(nextMipWidth, nextMipHeight, 1);
            blit.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            blit.dstSubresource().baseArrayLayer(0);
            blit.dstSubresource().layerCount(1);
            blit.dstSubresource().mipLevel(mipLevel);

            vkCmdBlitImage(
                commandBuffer,
                image,
                getLayout(mipLevel - 1),
                image,
                getLayout(mipLevel),
                blit,
                VK_FILTER_LINEAR
            );

            mipWidth = nextMipWidth;
            mipHeight = nextMipHeight;
        }

        for (int mipLevel = 0; mipLevel < mipLevels; mipLevel++) {
            transitionLayout(
                commandBuffer,
                tmpLayouts[mipLevel],
                VK_PIPELINE_STAGE_TRANSFER_BIT,
                VK_ACCESS_TRANSFER_WRITE_BIT,
                VK_PIPELINE_STAGE_ALL_COMMANDS_BIT,
                0,
                mipLevel,
                stack
            );
        }
    }

    public void transitionLayout(
        VkCommandBuffer commandBuffer,
        int newLayout,
        int srcStage,
        int srcAccessMask,
        int dstStage,
        int dstAccessMask,
        MemoryStack stack
    ) {
        transitionLayout(commandBuffer, newLayout, srcStage, srcAccessMask, dstStage, dstAccessMask, 0, mipLevels, stack);
    }

    public void transitionLayout(
        VkCommandBuffer commandBuffer,
        int newLayout,
        int srcStage,
        int srcAccessMask,
        int dstStage,
        int dstAccessMask,
        int baseMipLevel,
        MemoryStack stack
    ) {
        transitionLayout(commandBuffer, newLayout, srcStage, srcAccessMask, dstStage, dstAccessMask, baseMipLevel, 1, stack);
    }

    public void transitionLayout(
        VkCommandBuffer commandBuffer,
        int newLayout,
        int srcStage,
        int srcAccessMask,
        int dstStage,
        int dstAccessMask,
        int baseMipLevel,
        int levelCount,
        MemoryStack stack
    ) {
        VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1, stack);
        barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
        barrier.image(image);
        barrier.oldLayout(getLayout(baseMipLevel));
        barrier.newLayout(newLayout);
        barrier.srcAccessMask(srcAccessMask);
        barrier.dstAccessMask(dstAccessMask);
        barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
        barrier.subresourceRange().aspectMask(aspectMask);
        barrier.subresourceRange().baseMipLevel(baseMipLevel);
        barrier.subresourceRange().levelCount(levelCount);
        barrier.subresourceRange().layerCount(layers);

        vkCmdPipelineBarrier(commandBuffer, srcStage, dstStage, 0, null, null, barrier);

        for (int layer = 0; layer < layers; layer++) {
            for (int mipLevel = baseMipLevel; mipLevel < (baseMipLevel + levelCount); mipLevel++) {
                layouts[layer][mipLevel] = newLayout;
            }
        }
    }

    public int getLayout(int mipLevel) {
        return getLayout(0, mipLevel);
    }

    public int getLayout(int layer, int mipLevel) {
        return layouts[layer][mipLevel];
    }

    public void setAutomaticallyTransitionedLayout(int layout) {
        automaticallyTransitionedLayouts = new int[layers][mipLevels];
        for (int layer = 0; layer < layers; layer++) {
            for (int mipLevel = 0; mipLevel < mipLevels; mipLevel++) {
                automaticallyTransitionedLayouts[layer][mipLevel] = layout;
            }
        }
    }

    public int getLayoutForDescriptor() {
        return (automaticallyTransitionedLayouts != null) ? automaticallyTransitionedLayouts[0][0] : layouts[0][0];
    }

    public void createImageView(MemoryStack stack) {
        createImageView(VK_IMAGE_VIEW_TYPE_2D, stack);
    }

    public void createImageView(int viewType, MemoryStack stack) {
        imageView = createSeparateImageView(viewType, 0, layers, stack);
    }

    public long createSeparateImageView(int viewType, int baseArrayLayer, int layerCount, MemoryStack stack) {
        VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
        viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
        viewInfo.image(image);
        viewInfo.format(format);
        viewInfo.viewType(viewType);
        viewInfo.subresourceRange().aspectMask(aspectMask);
        viewInfo.subresourceRange().baseArrayLayer(baseArrayLayer);
        viewInfo.subresourceRange().layerCount(layerCount);
        viewInfo.subresourceRange().levelCount(mipLevels);

        LongBuffer pImageView = stack.mallocLong(1);
        int result = vkCreateImageView(application.getLogicalDevice(), viewInfo, null, pImageView);
        if (result != VK_SUCCESS) {
            throw new RuntimeException("Failed to create image view (result = " + result + ")");
        }
        return pImageView.get(0);
    }

    public void createSampler(int addressMode, MemoryStack stack) {
        createSampler(addressMode, 16, VK_BORDER_COLOR_INT_OPAQUE_BLACK, VK_SAMPLER_MIPMAP_MODE_LINEAR, stack);
    }

    public void createSampler(int addressMode, Integer maxAnisotropy, int borderColor, int mipMapMode, MemoryStack stack) {
        VkSamplerCreateInfo samplerCreateInfo = VkSamplerCreateInfo.callocStack(stack);
        samplerCreateInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
        samplerCreateInfo.magFilter(VK_FILTER_LINEAR);
        samplerCreateInfo.minFilter(VK_FILTER_LINEAR);
        samplerCreateInfo.addressModeU(addressMode);
        samplerCreateInfo.addressModeV(addressMode);
        samplerCreateInfo.addressModeW(addressMode);
        if (maxAnisotropy != null) {
            samplerCreateInfo.anisotropyEnable(true);
            samplerCreateInfo.maxAnisotropy(maxAnisotropy);
        }
        samplerCreateInfo.borderColor(borderColor);
        samplerCreateInfo.mipmapMode(mipMapMode);
        samplerCreateInfo.maxLod(mipLevels);

        LongBuffer pSampler = stack.mallocLong(1);
        int result = vkCreateSampler(application.getLogicalDevice(), samplerCreateInfo, null, pSampler);
        if (result != VK_SUCCESS) {
            throw new RuntimeException("Failed to create sampler (result = " + result + ")");
        }
        sampler = pSampler.get(0);
    }

    @Override
    protected void cleanupNativeInternal() {
        if (sampler != null) {
            vkDestroySampler(application.getLogicalDevice(), sampler, null);
            sampler = null;
        }
        if (imageView != null) {
            vkDestroyImageView(application.getLogicalDevice(), imageView, null);
            imageView = null;
        }
        if (image != null) {
            vmaDestroyImage(application.getMemoryManager().getAllocator(), image, imageAllocation);
            image = null;
            imageAllocation = null;
        }
        super.cleanupNativeInternal();
    }

    @Override
    public Texture clone(CloneContext context) {
        throw new UnsupportedOperationException("Texture should be reused instead of cloned!");
    }
}
