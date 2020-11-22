package com.destrostudios.icetea.core.materials.descriptors;

import com.destrostudios.icetea.core.MaterialDescriptor;
import com.destrostudios.icetea.core.Texture;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public abstract class TextureDescriptor<LayoutType extends TextureDescriptorLayout> extends MaterialDescriptor<LayoutType> {

    public TextureDescriptor(String name, LayoutType layout, Texture texture, int imageLayout) {
        super(name, layout);
        this.texture = texture;
        this.imageLayout = imageLayout;
    }
    private Texture texture;
    private int imageLayout;

    @Override
    public void initReferenceDescriptorWrite(VkWriteDescriptorSet descriptorWrite, MemoryStack stack) {
        super.initReferenceDescriptorWrite(descriptorWrite, stack);
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
        imageInfo.imageLayout(imageLayout);
        imageInfo.imageView(texture.getImageView());
        imageInfo.sampler(texture.getImageSampler());
        descriptorWrite.pImageInfo(imageInfo);
    }

    @Override
    protected String getShaderDeclarationType() {
        return "sampler2D";
    }
}
