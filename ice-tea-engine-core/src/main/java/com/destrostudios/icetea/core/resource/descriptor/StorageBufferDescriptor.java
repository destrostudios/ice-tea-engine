package com.destrostudios.icetea.core.resource.descriptor;

import com.destrostudios.icetea.core.buffer.StorageDataBuffer;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.data.values.DataValue;

import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_DESCRIPTOR_TYPE_STORAGE_BUFFER;

public class StorageBufferDescriptor extends MemoryBufferDataDescriptor<StorageDataBuffer> {

    public StorageBufferDescriptor(int stageFlags) {
        super(VK_DESCRIPTOR_TYPE_STORAGE_BUFFER, stageFlags);
    }

    public StorageBufferDescriptor(StorageBufferDescriptor storageBufferDescriptor, CloneContext context) {
        super(storageBufferDescriptor, context);
    }

    @Override
    protected String getShaderDeclaration_Type(String name) {
        String type = "";
        type += "buffer " + name.toUpperCase() + "_TYPE {\n";
        for (Map.Entry<String, DataValue<?>> field : resource.getData().getFields().entrySet()) {
            type += "    " + field.getValue().getShaderDefinitionType() + " " + field.getKey() + ";\n";
        }
        type += "}";
        return type;
    }

    @Override
    public StorageBufferDescriptor clone(CloneContext context) {
        return new StorageBufferDescriptor(this, context);
    }
}