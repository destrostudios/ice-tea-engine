package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.data.values.DataValue;

import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;

public class UniformDescriptor extends MemoryBufferDataDescriptor<UniformDataBuffer> {

    public UniformDescriptor(int stageFlags) {
        super(stageFlags);
    }

    public UniformDescriptor(UniformDescriptor uniformDescriptor, CloneContext context) {
        super(uniformDescriptor, context);
    }

    @Override
    protected int getDescriptorType() {
        return VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
    }

    @Override
    protected String getShaderDeclaration_Type(String name) {
        String type = "uniform " + name.toUpperCase() + "_TYPE {\n";
        for (Map.Entry<String, DataValue<?>> field : resource.getData().getFields().entrySet()) {
            type += "    " + field.getValue().getShaderDefinitionType() + " " + field.getKey() + ";\n";
        }
        type += "}";
        return type;
    }

    @Override
    protected List<String> getShaderDeclaration_Defines(String name) {
        List<String> defines = super.getShaderDeclaration_Defines(name);
        for (String fieldName : resource.getData().getFields().keySet()) {
            defines.add(name.toUpperCase() + "_" + fieldName.toUpperCase());
        }
        return defines;
    }

    @Override
    public UniformDescriptor clone(CloneContext context) {
        return new UniformDescriptor(this, context);
    }
}