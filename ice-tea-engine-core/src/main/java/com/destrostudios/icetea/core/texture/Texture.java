package com.destrostudios.icetea.core.texture;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import lombok.Getter;

import static org.lwjgl.vulkan.VK10.*;

public class Texture extends LifecycleObject {

    public Texture() {

    }

    public Texture(long image, long imageMemory, long imageView, int imageViewLayout) {
        this.image = image;
        this.imageMemory = imageMemory;
        this.imageView = imageView;
        this.imageViewLayout = imageViewLayout;
    }

    public Texture(long image, long imageMemory, long imageView, int imageViewLayout, long imageSampler) {
        this.image = image;
        this.imageMemory = imageMemory;
        this.imageView = imageView;
        this.imageViewLayout = imageViewLayout;
        this.imageSampler = imageSampler;
    }
    @Getter
    protected Long image;
    @Getter
    protected Long imageMemory;
    @Getter
    protected Long imageView;
    @Getter
    protected int imageViewLayout;
    @Getter
    protected Long imageSampler;

    @Override
    public void cleanup() {
        if (imageSampler != null) {
            vkDestroySampler(application.getLogicalDevice(), imageSampler, null);
            imageSampler = null;
        }
        if (imageView != null) {
            vkDestroyImageView(application.getLogicalDevice(), imageView, null);
            imageView = null;
        }
        if (image != null) {
            vkDestroyImage(application.getLogicalDevice(), image, null);
            image = null;
        }
        if (imageMemory != null) {
            vkFreeMemory(application.getLogicalDevice(), imageMemory, null);
            imageMemory = null;
        }
        super.cleanup();
    }
}
