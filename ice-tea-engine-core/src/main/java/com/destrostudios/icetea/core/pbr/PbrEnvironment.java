package com.destrostudios.icetea.core.pbr;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.object.LogicalObject;
import com.destrostudios.icetea.core.resource.descriptor.PbrEnvironmentDescriptor;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class PbrEnvironment extends LogicalObject {

    public PbrEnvironment(Texture environmentMap, Texture irradianceMap, Texture prefilteredEnvironmentMap, Texture brdfLookupTexture, boolean applyToneMapping) {
        this.environmentMap = environmentMap;
        this.irradianceMap = irradianceMap;
        this.prefilteredEnvironmentMap = prefilteredEnvironmentMap;
        this.brdfLookupTexture = brdfLookupTexture;
        this.applyToneMapping = applyToneMapping;
        uniformBuffer = new UniformDataBuffer();
        uniformBuffer.setDescriptor("default", new PbrEnvironmentDescriptor());
    }
    private Texture environmentMap;
    private Texture irradianceMap;
    private Texture prefilteredEnvironmentMap;
    private Texture brdfLookupTexture;
    private boolean applyToneMapping;
    @Getter
    private UniformDataBuffer uniformBuffer;

    @Override
    public void applyLogicalState() {
        super.applyLogicalState();
        uniformBuffer.getData().setInt("prefilteredEnvironmentMapMipLevels", prefilteredEnvironmentMap.getMipLevels());
        uniformBuffer.getData().setBoolean("applyToneMapping", applyToneMapping);
    }

    @Override
    public void updateNativeState(Application application) {
        super.updateNativeState(application);
        environmentMap.updateNative(application);
        irradianceMap.updateNative(application);
        prefilteredEnvironmentMap.updateNative(application);
        brdfLookupTexture.updateNative(application);
        uniformBuffer.updateNative(application);
    }

    @Override
    public void cleanupNativeStateInternal() {
        uniformBuffer.cleanupNative();
        brdfLookupTexture.cleanupNative();
        prefilteredEnvironmentMap.cleanupNative();
        irradianceMap.cleanupNative();
        environmentMap.cleanupNative();
        super.cleanupNativeStateInternal();
    }
}
