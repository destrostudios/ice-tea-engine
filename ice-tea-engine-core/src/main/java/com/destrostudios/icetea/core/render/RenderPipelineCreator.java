package com.destrostudios.icetea.core.render;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Pipeline;
import com.destrostudios.icetea.core.resource.ResourceDescriptorSet;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.shader.ShaderType;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkVertexInputAttributeDescription;
import org.lwjgl.vulkan.VkVertexInputBindingDescription;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.VK10.VK_VERTEX_INPUT_RATE_VERTEX;

public abstract class RenderPipelineCreator<RJ extends RenderJob<?>, PS extends PipelineState> {

    public RenderPipelineCreator(Application application, RJ renderJob) {
        this.application = application;
        this.renderJob = renderJob;
    }
    protected Application application;
    protected RJ renderJob;

    public Pipeline getOrCreatePipeline(GeometryRenderContext geometryRenderContext) {
        PS state = (PS) createState(geometryRenderContext);
        return getOrCreatePipeline(state, geometryRenderContext.getResourceDescriptorSet());
    }

    public Pipeline getOrCreatePipeline(PS state, ResourceDescriptorSet resourceDescriptorSet) {
        return application.getPipelineManager().getOrCreate(state, s -> {
            try (MemoryStack stack = stackPush()) {
                return createPipeline(s, resourceDescriptorSet.getDescriptorSetLayouts(stack), stack);
            }
        });
    }

    protected abstract PS createState(GeometryRenderContext<RJ> geometryRenderContext);

    protected abstract Pipeline createPipeline(PS state, LongBuffer descriptorSetLayouts, MemoryStack stack);

    protected long createShaderModule_Vertex(Shader shader, String additionalDeclarations, List<EssentialGeometryRenderPipelineState.VertexField> vertexFields) {
        return createShaderModule(shader, ShaderType.VERTEX_SHADER, additionalDeclarations + getVertexDataDeclarations(vertexFields));
    }

    protected long createShaderModule(Shader shader, ShaderType shaderType, String additionalDeclarations) {
        return application.getShaderManager().createShaderModule(shader, shaderType, additionalDeclarations + getRenderJobDeclarations());
    }

    private String getRenderJobDeclarations() {
        return "#define RENDERJOB_" + renderJob.getName().toUpperCase() + " 1\n";
    }

    private static String getVertexDataDeclarations(List<EssentialGeometryRenderPipelineState.VertexField> vertexFields) {
        String text = "";
        int location = 0;
        for (EssentialGeometryRenderPipelineState.VertexField vertexField : vertexFields) {
            text += "#define VERTEX_" + vertexField.getName().toUpperCase() + " 1\n";
            text += "layout(location = " + location + ") in " + vertexField.getShaderDefinitionType() + " " + vertexField.getName() + ";\n";
            location++;
        }
        return text;
    }

    protected static VkVertexInputBindingDescription.Buffer getVertexBindingDescriptions(int vertexSize) {
        VkVertexInputBindingDescription.Buffer bindingDescription = VkVertexInputBindingDescription.callocStack(1);
        bindingDescription.binding(0);
        bindingDescription.stride(vertexSize);
        bindingDescription.inputRate(VK_VERTEX_INPUT_RATE_VERTEX);
        return bindingDescription;
    }

    protected static VkVertexInputAttributeDescription.Buffer getVertexAttributeDescriptions(List<EssentialGeometryRenderPipelineState.VertexField> vertexFields) {
        VkVertexInputAttributeDescription.Buffer attributeDescriptions = VkVertexInputAttributeDescription.callocStack(vertexFields.size());
        int offset = 0;
        int location = 0;
        for (EssentialGeometryRenderPipelineState.VertexField vertexField : vertexFields) {
            VkVertexInputAttributeDescription attributeDescription = attributeDescriptions.get(location);
            attributeDescription.binding(0);
            attributeDescription.location(location);
            attributeDescription.format(vertexField.getFormat());
            attributeDescription.offset(offset);
            location++;
            offset += vertexField.getSize();
        }
        return attributeDescriptions.rewind();
    }
}
