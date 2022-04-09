package com.destrostudios.icetea.core.material;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.texture.Texture;
import com.destrostudios.icetea.core.data.UniformData;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.function.Supplier;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;
import static org.lwjgl.vulkan.VK10.VK_POLYGON_MODE_FILL;

public class Material implements ContextCloneable {

    public Material() {
        parameters = new UniformData();
    }

    public Material(Material material, CloneContext context) {
        vertexShader = material.vertexShader;
        fragmentShader = material.fragmentShader;
        tessellationPatchSize = material.tessellationPatchSize;
        tessellationControlShader = material.tessellationControlShader;
        tessellationEvaluationShader = material.tessellationEvaluationShader;
        geometryShader = material.geometryShader;
        parameters = material.parameters.clone(context);
        textureSuppliers.putAll(material.getTextureSuppliers());
        transparent = material.transparent;
        cullMode = material.cullMode;
        depthTest = material.depthTest;
        depthWrite = material.depthWrite;
        fillMode = material.fillMode;
    }
    private Application application;
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
    private UniformData parameters;
    @Getter
    private HashMap<String, Supplier<Texture>> textureSuppliers = new HashMap<>();
    private int usingGeometriesCount;
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

    public boolean isInitialized() {
        return (application != null);
    }

    public void init(Application application) {
        this.application = application;
        for (Supplier<Texture> textureSupplier : textureSuppliers.values()) {
            Texture texture = textureSupplier.get();
            if (!texture.isInitialized()) {
                texture.init(application);
            }
        }
    }

    public void setTexture(String name, Texture texture) {
        setTexture(name, () -> texture);
    }

    public void setTexture(String name, Supplier<Texture> textureSupplier) {
        textureSuppliers.put(name, textureSupplier);
    }

    public void increaseUsingGeometriesCount() {
        usingGeometriesCount++;
    }

    public void decreaseUsingGeometriesCount() {
        usingGeometriesCount--;
    }

    public boolean isUnused() {
        return (usingGeometriesCount <= 0);
    }

    public void cleanup() {
        parameters.cleanupBuffer();
        for (Supplier<Texture> textureSupplier : textureSuppliers.values()) {
            Texture texture = textureSupplier.get();
            // Can already be cleanuped by the provider (e.g. the responsible render job)
            if (texture != null) {
                texture.cleanup();
            }
        }
    }

    @Override
    public Material clone(CloneContext context) {
        return new Material(this, context);
    }
}
