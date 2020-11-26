package com.destrostudios.icetea.core;

import lombok.Getter;

import static org.lwjgl.vulkan.VK10.*;

public class Texture {

    public Texture() {

    }

    public Texture(long image, long imageMemory, long imageView) {
        this.image = image;
        this.imageMemory = imageMemory;
        this.imageView = imageView;
    }

    public Texture(long image, long imageMemory, long imageView, long imageSampler) {
        this.image = image;
        this.imageMemory = imageMemory;
        this.imageView = imageView;
        this.imageSampler = imageSampler;
    }
    protected Application application;
    @Getter
    protected Long image;
    @Getter
    protected Long imageMemory;
    @Getter
    protected Long imageView;
    @Getter
    protected Long imageSampler;

    public boolean isInitialized() {
        return (application != null);
    }

    public void init(Application application) {
        this.application = application;
    }

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
    }
}
