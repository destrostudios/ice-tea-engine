package com.destrostudios.icetea.samples.whoosh;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.scene.Control;
import com.destrostudios.icetea.core.scene.Geometry;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class WhooshControl extends Control {

    public WhooshControl(WhooshConfig whooshConfig) {
        this.whooshConfig = whooshConfig;
    }
    private WhooshConfig whooshConfig;
    private float time;

    @Override
    protected void onAttached() {
        super.onAttached();
        Geometry geometry = (Geometry) spatial;

        Vector3f initialPosition = new Vector3f(geometry.getWorldTransform().getTranslation());

        Material material = geometry.getMaterial().clone(CloneContext.reuseAll());
        material.addShaderNodes("whoosh");
        // FIXME: Vectors have to be defined first or somehow the memory alignment is messed up
        material.getParameters().setVector3f("meshBoundsCenter", geometry.getMesh().getBounds().getCenter());
        material.getParameters().setVector3f("meshBoundsExtent", geometry.getMesh().getBounds().getExtent());
        material.getParameters().setVector3f("targetPositionOld", initialPosition);
        material.getParameters().setVector3f("targetPositionNew", initialPosition);
        // FIXME: Needs to be added here or the memory alignment is messed up
        material.getParameters().setVector4f("fixMeMemoryFix", new Vector4f());
        material.getParameters().setFloat("duration", whooshConfig.getDuration());
        material.getParameters().setFloat("startTime", 0f);
        material.getParameters().setFloat("time", 0f);
        geometry.setMaterial(material);
    }

    @Override
    public void updateLogicalState(Application application, float tpf) {
        super.updateLogicalState(application, tpf);
        time += tpf;
        Geometry geometry = (Geometry) spatial;
        geometry.getMaterial().getParameters().setFloat("time", time);
    }

    public void whoosh(Vector3f targetPosition) {
        Material material = ((Geometry) spatial).getMaterial();
        material.getParameters().setVector3f("targetPositionOld", material.getParameters().getVector3f("targetPositionNew"));
        material.getParameters().setVector3f("targetPositionNew", targetPosition);
        material.getParameters().setFloat("startTime", time);
    }

    @Override
    public Control clone(CloneContext context) {
        throw new UnsupportedOperationException();
    }
}
