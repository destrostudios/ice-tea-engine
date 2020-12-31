package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.data.VertexData;
import com.destrostudios.icetea.core.data.values.UniformValue;
import com.destrostudios.icetea.core.mesh.Mesh;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

public abstract class RenderPipeline<RJ extends RenderJob<?>> extends Pipeline {

    public RenderPipeline(Application application, RJ renderJob) {
        super(application);
        this.renderJob = renderJob;
    }
    protected RJ renderJob;

    protected static VkVertexInputBindingDescription.Buffer getBindingDescriptions(Mesh mesh) {
        VertexData referenceVertex = mesh.getVertices()[0];

        VkVertexInputBindingDescription.Buffer bindingDescription =  VkVertexInputBindingDescription.callocStack(1);
        bindingDescription.binding(0);
        bindingDescription.stride(referenceVertex.getSize());
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
        return bindingDescription;
    }

    protected static VkVertexInputAttributeDescription.Buffer getAttributeDescriptions(Mesh mesh) {
        VertexData referenceVertex = mesh.getVertices()[0];

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
}
