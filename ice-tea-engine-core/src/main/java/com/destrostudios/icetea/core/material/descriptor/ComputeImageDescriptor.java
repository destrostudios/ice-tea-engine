package com.destrostudios.icetea.core.material.descriptor;

import com.destrostudios.icetea.core.texture.Texture;
import lombok.Getter;

import java.util.List;

public class ComputeImageDescriptor extends TextureDescriptor {

    public ComputeImageDescriptor(String name,Texture texture, String format, boolean writeOnly) {
        super(name, texture);
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
