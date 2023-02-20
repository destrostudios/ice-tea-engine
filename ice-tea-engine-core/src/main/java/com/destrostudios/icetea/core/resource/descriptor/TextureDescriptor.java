package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public abstract class TextureDescriptor extends ResourceDescriptor<Texture> {

    public TextureDescriptor(int stageFlags, boolean isArray) {
        this.stageFlags = stageFlags;
        this.isArray = isArray;
    }

    public TextureDescriptor(TextureDescriptor textureDescriptor, CloneContext context) {
        super(textureDescriptor, context);
        stageFlags = textureDescriptor.stageFlags;
        isArray = textureDescriptor.isArray;
    }
    private int stageFlags;
    private boolean isArray;

    @Override
    protected void initDescriptorSetLayoutBinding(VkDescriptorSetLayoutBinding.Buffer layoutBinding) {
        super.initDescriptorSetLayoutBinding(layoutBinding);
        layoutBinding.stageFlags(stageFlags);
    }

    @Override
    protected void initWriteDescriptorSet(VkWriteDescriptorSet descriptorWrite, MemoryStack stack) {
        super.initWriteDescriptorSet(descriptorWrite, stack);
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.callocStack(1, stack);
        imageInfo.imageLayout(resource.getImageViewLayout());
        imageInfo.imageView(resource.getImageView());
        imageInfo.sampler(resource.getImageSampler());
        descriptorWrite.pImageInfo(imageInfo);
    }

    @Override
    protected String getShaderDeclaration_Type(String name) {
        return "uniform " + (isArray ? "sampler2DArray" : "sampler2D");
    }

    @Override
    public abstract TextureDescriptor clone(CloneContext context);
}
