package com.destrostudios.icetea.core;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.util.LinkedList;
import java.util.List;

public abstract class Light {

    public Light() {
        affectedSpatials = new LinkedList<>();
        shadowMapRenderJobs = new LinkedList<>();
        lightColor = new Vector4f(1, 1, 1, 1);
        ambientColor = new Vector4f(0.1f, 0.1f, 0.1f, 1);
        specularColor = new Vector4f(1, 1, 1, 1);
        uniformData = new UniformData();
    }
    private Application application;
    @Getter
    private List<Spatial> affectedSpatials;
    @Getter
    private List<ShadowMapRenderJob> shadowMapRenderJobs;
    @Getter
    @Setter
    private Vector4f lightColor;
    @Getter
    @Setter
    private Vector4f ambientColor;
    @Getter
    @Setter
    private Vector4f specularColor;
    @Getter
    protected UniformData uniformData;
    @Getter
    @Setter
    private boolean modified;

    public void update(Application application) {
        if (this.application == null) {
            uniformData.setApplication(application);
            updateUniformDataFields();
            uniformData.initBuffer();
            this.application = application;
        }
    }

    public void updateUniformBuffers(int currentImage, MemoryStack stack) {
        updateUniformDataFields();
        uniformData.updateBufferIfNecessary(currentImage, stack);
    }

    protected void updateUniformDataFields() {
        uniformData.setVector4f("lightColor", lightColor);
        uniformData.setVector4f("ambientColor", ambientColor);
        uniformData.setVector4f("specularColor", specularColor);
    }

    public void addAffectedSpatial(Spatial spatial) {
        affectedSpatials.add(spatial);
        modified = true;
    }

    public void removeAffectedSpatial(Spatial spatial) {
        affectedSpatials.add(spatial);
        modified = true;
    }

    public void addShadows(int shadowMapSize) {
        shadowMapRenderJobs.add(new ShadowMapRenderJob(this, shadowMapSize));
        modified = true;
    }

    public boolean isAffecting(Spatial spatial) {
        return affectedSpatials.stream().anyMatch(affectedSpatial -> isAffecting(affectedSpatial, spatial));
    }

    private boolean isAffecting(Spatial affectedSpatial, Spatial spatialToCheck) {
        if (affectedSpatial == spatialToCheck) {
            return true;
        }
        if (affectedSpatial instanceof Node) {
            Node node = (Node) affectedSpatial;
            return node.getChildren().stream().anyMatch(child -> isAffecting(child, spatialToCheck));
        }
        return false;
    }

    public void cleanup() {
        uniformData.cleanupBuffer();
    }
}
