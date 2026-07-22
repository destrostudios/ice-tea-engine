package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

public abstract class TextureDescriptor extends ResourceDescriptor<Texture> {

    public TextureDescriptor(int descriptorType, int stageFlags, boolean isCubeMap, boolean isArray) {
        super(descriptorType, stageFlags);
        this.isCubeMap = isCubeMap;
        this.isArray = isArray;
    }

    public TextureDescriptor(TextureDescriptor textureDescriptor, CloneContext context) {
        super(textureDescriptor, context);
        isCubeMap = textureDescriptor.isCubeMap;
        isArray = textureDescriptor.isArray;
    }
    private boolean isCubeMap;
    private boolean isArray;

    @Override
    protected void initWriteDescriptorSet(VkWriteDescriptorSet descriptorWrite, MemoryStack stack) {
        super.initWriteDescriptorSet(descriptorWrite, stack);
        VkDescriptorImageInfo.Buffer imageInfo = VkDescriptorImageInfo.calloc(1, stack);
        imageInfo.imageLayout(resource.getLayoutForDescriptor());
        imageInfo.imageView(resource.getImageView());
        imageInfo.sampler(resource.getSampler());
        descriptorWrite.pImageInfo(imageInfo);
    }

    @Override
    protected String getShaderDeclaration_Type(String name) {
        return "uniform sampler" + (isCubeMap ? "Cube" : "2D") + (isArray ? "Array" : "");
    }

    @Override
    public abstract TextureDescriptor clone(CloneContext context);
}
