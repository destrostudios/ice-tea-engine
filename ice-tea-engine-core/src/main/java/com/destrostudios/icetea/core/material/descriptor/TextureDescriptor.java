package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.texture.Texture;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public abstract class TextureDescriptor extends MaterialDescriptor {

    public TextureDescriptor(String name, Texture texture, int imageLayout) {
        super(name);
        this.texture = texture;
        this.imageLayout = imageLayout;
    }
    private Texture texture;
    private int imageLayout;

    @Override
    public void initReferenceDescriptorWrite(VkWriteDescriptorSet descriptorWrite, MaterialDescriptorLayout layout, MemoryStack stack) {
        super.initReferenceDescriptorWrite(descriptorWrite, layout, stack);
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
        imageInfo.imageLayout(imageLayout);
        imageInfo.imageView(texture.getImageView());
        imageInfo.sampler(texture.getImageSampler());
        descriptorWrite.pImageInfo(imageInfo);
    }

    @Override
    protected String getShaderDeclaration_Type() {
        return "uniform sampler2D";
    }
}
