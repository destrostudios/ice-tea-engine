package com.destrostudios.icetea.core.texture;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.resource.Resource;
import lombok.Getter;

import static org.lwjgl.vulkan.VK10.*;

public class Texture extends Resource {

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

    private boolean wasJustSet;

    public void set(long image, long imageMemory, long imageView, int imageViewLayout) {
        set(image, imageMemory, imageView, imageViewLayout, null);
    }

    public void set(long image, long imageMemory, long imageView, int imageViewLayout, Long imageSampler) {
        this.image = image;
        this.imageMemory = imageMemory;
        this.imageView = imageView;
        this.imageViewLayout = imageViewLayout;
        this.imageSampler = imageSampler;
        onSet();
    }

    protected void onSet() {
        wasJustSet = true;
    }

    @Override
    protected void updateResource(float tpf) {
        if (wasJustSet) {
            setWasOutdated();
            wasJustSet = false;
        }
    }

    @Override
    protected void cleanupInternal() {
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
        super.cleanupInternal();
    }

    @Override
    public Texture clone(CloneContext context) {
        throw new UnsupportedOperationException("Texture should be reused instead of cloned!");
    }
}
