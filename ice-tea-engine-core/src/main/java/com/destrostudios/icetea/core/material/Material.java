package com.destrostudios.icetea.core.material;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.Texture;
import com.destrostudios.icetea.core.data.UniformData;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.function.Supplier;

import static org.lwjgl.vulkan.VK10.VK_CULL_MODE_BACK_BIT;

public class Material {

    public Material() {
        parameters = new UniformData();
        textureSuppliers = new HashMap<>();
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
    private int tesselationPatchSize;
    @Getter
    @Setter
    private Shader tesselationControlShader;
    @Getter
    @Setter
    private Shader tesselationEvaluationShader;
    @Getter
    @Setter
    private Shader geometryShader;
    @Getter
    private UniformData parameters;
    @Getter
    private HashMap<String, Supplier<Texture>> textureSuppliers;
    private int usingGeometriesCount;
    @Setter
    @Getter
    private boolean transparent;
    @Setter
    @Getter
    private int cullMode = VK_CULL_MODE_BACK_BIT;

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
}
