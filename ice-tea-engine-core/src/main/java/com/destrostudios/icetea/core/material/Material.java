package com.destrostudios.icetea.core.material;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.data.FieldsData;
import com.destrostudios.icetea.core.lifecycle.MultiConsumableLifecycleObject;
import com.destrostudios.icetea.core.resource.descriptor.MaterialParamsDescriptor;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;

public class Material extends MultiConsumableLifecycleObject<Geometry> implements ContextCloneable {

    public Material() {
        parametersBuffer = new UniformDataBuffer();
        parametersBuffer.setDescriptor("default", new MaterialParamsDescriptor());
    }

    public Material(Material material, CloneContext context) {
        vertexShader = material.vertexShader;
        fragmentShader = material.fragmentShader;
        tessellationPatchSize = material.tessellationPatchSize;
        tessellationControlShader = material.tessellationControlShader;
        tessellationEvaluationShader = material.tessellationEvaluationShader;
        geometryShader = material.geometryShader;
        parametersBuffer = material.parametersBuffer.clone(context);
        textures.putAll(material.getTextures());
        transparent = material.transparent;
        cullMode = material.cullMode;
        depthTest = material.depthTest;
        depthWrite = material.depthWrite;
        fillMode = material.fillMode;
    }
    @Getter
    @Setter
    private Shader vertexShader;
    @Getter
    @Setter
    private Shader fragmentShader;
    @Setter
    @Getter
    private int tessellationPatchSize;
    @Getter
    @Setter
    private Shader tessellationControlShader;
    @Getter
    @Setter
    private Shader tessellationEvaluationShader;
    @Getter
    @Setter
    private Shader geometryShader;
    @Getter
    private UniformDataBuffer parametersBuffer;
    @Getter
    private HashMap<String, Texture> textures = new HashMap<>();
    @Setter
    @Getter
    private boolean transparent;
    @Setter
    @Getter
    private int cullMode = VK_CULL_MODE_BACK_BIT;
    @Setter
    @Getter
    private boolean depthTest = true;
    @Setter
    @Getter
    private boolean depthWrite = true;
    @Setter
    @Getter
    private int fillMode = VK_POLYGON_MODE_FILL;

    @Override
    protected void update(float tpf) {
        super.update(tpf);
        application.getSwapChain().setResourceActive(parametersBuffer);
        for (Texture texture : textures.values()) {
            application.getSwapChain().setResourceActive(texture);
        }
    }

    public FieldsData getParameters() {
        return parametersBuffer.getData();
    }

    public void setTexture(String name, Texture texture) {
        textures.put(name, texture);
    }

    @Override
    protected void cleanupInternal() {
        parametersBuffer.cleanup();
        for (Texture texture : textures.values()) {
            texture.cleanup();
        }
        super.cleanupInternal();
    }

    @Override
    public Material clone(CloneContext context) {
        return new Material(this, context);
    }
}
