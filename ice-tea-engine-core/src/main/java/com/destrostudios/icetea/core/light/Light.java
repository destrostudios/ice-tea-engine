package com.destrostudios.icetea.core.light;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.object.LogicalObject;
import com.destrostudios.icetea.core.render.shadow.ShadowConfig;
import com.destrostudios.icetea.core.resource.descriptor.LightDescriptor;
import com.destrostudios.icetea.core.render.shadow.ShadowMapRenderJob;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

public abstract class Light extends LogicalObject {

    public Light() {
        lightColor = new Vector4f(1, 1, 1, 1);
        ambientColor = new Vector4f(0.1f, 0.1f, 0.1f, 1);
        specularColor = new Vector4f(1, 1, 1, 1);
        uniformBuffer = new UniformDataBuffer();
        uniformBuffer.setDescriptor("default", new LightDescriptor());
    }
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
    protected UniformDataBuffer uniformBuffer;
    @Getter
    private ShadowMapRenderJob shadowMapRenderJob;
    @Setter
    private boolean modified;

    @Override
    public void updateLogicalState(Application application, float tpf) {
        super.updateLogicalState(application, tpf);
        if (modified) {
            // TODO: Solve this properly, including updates
            if (shadowMapRenderJob != null) {
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
        uniformBuffer.getData().setVector4f("lightColor", lightColor);
        uniformBuffer.getData().setVector4f("ambientColor", ambientColor);
        uniformBuffer.getData().setVector4f("specularColor", specularColor);
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
