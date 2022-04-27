package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.data.values.UniformValue;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.shader.ShaderType;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import java.util.Map;

import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

public abstract class RenderPipeline<RJ extends RenderJob<?>> extends Pipeline {

    public RenderPipeline(RJ renderJob) {
        this.renderJob = renderJob;
    }
    protected RJ renderJob;

    protected long createShaderModule_Vertex(Shader shader, String additionalDeclarations, Mesh mesh) {
        return createShaderModule(shader, ShaderType.VERTEX_SHADER, additionalDeclarations + getVertexDataDeclarations(mesh) + "\n");
    }

    private static String getVertexDataDeclarations(Mesh mesh) {
        VertexData referenceVertex = getReferenceVertex(mesh);
        String text = "";
        int location = 0;
        for (Map.Entry<String, UniformValue<?>> field : referenceVertex.getFields().entrySet()) {
            text += "#define VERTEX_" + field.getKey().toUpperCase() + " 1\n";
            text += "layout(location = " + location + ") in " + field.getValue().getShaderDefinitionType() + " " + field.getKey() + ";\n";
            location++;
        }
        return text;
    }

    protected static VkVertexInputBindingDescription.Buffer getVertexBindingDescriptions(Mesh mesh) {
        VertexData referenceVertex = getReferenceVertex(mesh);
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.callocStack(1);
        bindingDescription.binding(0);
        bindingDescription.stride(referenceVertex.getSize());
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
        return bindingDescription;
    }

    protected static VkVertexInputAttributeDescription.Buffer getVertexAttributeDescriptions(Mesh mesh) {
        VertexData referenceVertex = getReferenceVertex(mesh);
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.callocStack(referenceVertex.getFields().size());
        int offset = 0;
        int location = 0;
        for (UniformValue<?> uniformValue : referenceVertex.getFields().values()) {
            VkVertexInputAttributeDescription attributeDescription = attributeDescriptions.get(location);
            attributeDescription.binding(0);
            attributeDescription.location(location);
            attributeDescription.format(uniformValue.getFormat());
            attributeDescription.offset(offset);
            location++;
            offset += uniformValue.getSize();
        }
        return attributeDescriptions.rewind();
    }

    private static VertexData getReferenceVertex(Mesh mesh) {
        return mesh.getVertices()[0];
    }
}
