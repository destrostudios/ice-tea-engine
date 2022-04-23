package com.destrostudios.icetea.core.light;

import com.destrostudios.icetea.core.lifecycle.LifecycleObject;
import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.scene.Node;
import com.destrostudios.icetea.core.scene.Spatial;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

import java.util.LinkedList;
import java.util.List;

public abstract class Light extends LifecycleObject {

    public Light() {
        affectedSpatials = new LinkedList<>();
        shadowMapRenderJobs = new LinkedList<>();
        lightColor = new Vector4f(1, 1, 1, 1);
        ambientColor = new Vector4f(0.1f, 0.1f, 0.1f, 1);
        specularColor = new Vector4f(1, 1, 1, 1);
        uniformData = new UniformData();
    }
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

    @Override
    public void update(int imageIndex, float tpf) {
        super.update(imageIndex, tpf);
        updateUniformDataFields();
        uniformData.update(application, imageIndex, tpf);
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

    @Override
    protected void cleanupInternal() {
        uniformData.cleanup();
        super.cleanupInternal();
    }
}
