package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;

import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_IMAGE_LAYOUT_GENERAL;

public class ComputeImageDescriptor extends TextureDescriptor<ComputeImageDescriptorLayout> {

    public ComputeImageDescriptor(String name, ComputeImageDescriptorLayout layout, Texture texture, String format, boolean writeOnly) {
        super(name, layout, texture, VK_IMAGE_LAYOUT_GENERAL);
        this.format = format;
        this.writeOnly = writeOnly;
    }
    @Getter
    private String format;
    @Getter
    private boolean writeOnly;

    @Override
    protected String getShaderDeclaration_LayoutAddition() {
        return format;
    }

    @Override
    protected List<String> getShaderDeclaration_Keywords() {
        List<String> keywords = super.getShaderDeclaration_Keywords();
        if (writeOnly) {
            keywords.add("writeonly");
        }
        return keywords;
    }

    @Override
    protected String getShaderDeclaration_Type() {
        return "uniform image2D";
    }
}
