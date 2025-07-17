package com.destrostudios.icetea.core.light;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.object.LogicalObject;
import com.destrostudios.icetea.core.render.shadow.ShadowConfig;
import com.destrostudios.icetea.core.resource.descriptor.LightDescriptor;
import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

public abstract class Light extends LogicalObject {

    public Light() {
        lightColor = new Vector3f(1, 1, 1);
        phongAmbientColor = new Vector3f(0.1f, 0.1f, 0.1f);
        phongSpecularColor = new Vector3f(1, 1, 1);
        uniformBuffer = new UniformDataBuffer();
        uniformBuffer.setDescriptor("default", new LightDescriptor());
    }
    @Getter
    @Setter
    private Vector3f lightColor;
    @Getter
    @Setter
    private Vector3f phongAmbientColor;
    @Getter
    @Setter
    private Vector3f phongSpecularColor;
    @Getter
    protected UniformDataBuffer uniformBuffer;
    @Getter
    private ShadowMapRenderJob shadowMapRenderJob;
    @Setter
    private boolean modified;

    @Override
    public void updateLogicalState(Application application, float tpf) {
        super.updateLogicalState(application, tpf);
        if (modified) {
            if (shadowMapRenderJob != null) {
                // TODO: Solve this properly, including updates
                application.getSwapChain().getRenderJobManager().addPreSceneRenderJob(shadowMapRenderJob);
            }
            modified = false;
        }
    }

    @Override
    public void applyLogicalState() {
        super.applyLogicalState();
        updateUniformBufferFields();
    }

    protected void updateUniformBufferFields() {
        uniformBuffer.getData().setVector3f("lightColor", lightColor);
        uniformBuffer.getData().setVector3f("phongAmbientColor", phongAmbientColor);
        uniformBuffer.getData().setVector3f("phongSpecularColor", phongSpecularColor);
    }

    @Override
    public void updateNativeState(Application application) {
        super.updateNativeState(application);
        uniformBuffer.updateNative(application);
    }

    public void enableShadows(ShadowConfig shadowConfig) {
        shadowMapRenderJob = new ShadowMapRenderJob(this, shadowConfig);
        modified = true;
    }

    @Override
    public void cleanupNativeStateInternal() {
        uniformBuffer.cleanupNative();
        super.cleanupNativeStateInternal();
    }
}
