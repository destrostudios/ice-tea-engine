package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.clone.CloneContext;
import lombok.Getter;

import java.util.List;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
import static org.lwjgl.vulkan.VK10.VK_SHADER_STAGE_COMPUTE_BIT;

public class ComputeImageDescriptor extends TextureDescriptor {

    public ComputeImageDescriptor(String format, boolean writeOnly) {
        super(VK_SHADER_STAGE_COMPUTE_BIT);
        this.format = format;
        this.writeOnly = writeOnly;
    }

    public ComputeImageDescriptor(ComputeImageDescriptor computeImageDescriptor, CloneContext context) {
        super(computeImageDescriptor, context);
        format = computeImageDescriptor.format;
        writeOnly = computeImageDescriptor.writeOnly;
    }
    @Getter
    private String format;
    @Getter
    private boolean writeOnly;

    @Override
    protected int getDescriptorType() {
        return VK_DESCRIPTOR_TYPE_STORAGE_IMAGE;
    }

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
    protected String getShaderDeclaration_Type(String name) {
        return "uniform image2D";
    }

    @Override
    public ComputeImageDescriptor clone(CloneContext context) {
        return new ComputeImageDescriptor(this, context);
    }
}
