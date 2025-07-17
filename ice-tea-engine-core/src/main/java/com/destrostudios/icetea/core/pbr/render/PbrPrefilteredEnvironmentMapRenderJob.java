package com.destrostudios.icetea.core.pbr.render;

import com.destrostudios.icetea.core.pbr.PbrConfig;
import com.destrostudios.icetea.core.render.cubemap.CubeMapRenderJob;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.texture.Texture;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R16G16B16A16_SFLOAT;

public class PbrPrefilteredEnvironmentMapRenderJob extends CubeMapRenderJob {

    private static Shader FRAGMENT_SHADER = new FileShader("com/destrostudios/icetea/core/shaders/pbr/prefilteredEnvironmentMap.frag");

    public PbrPrefilteredEnvironmentMapRenderJob(Texture environmentMap, PbrConfig pbrConfig) {
        this.environmentMap = environmentMap;
        this.pbrConfig = pbrConfig;
        cubeMapConfig.setSize(pbrConfig.getPrefilteredEnvironmentMapSize());
        cubeMapConfig.setFormat(VK_FORMAT_R16G16B16A16_SFLOAT);
        cubeMapConfig.setGenerateMipMaps(true);
        // Don't cleanup the cube texture which was passed to the created PbrEnvironment (which owns and controls its lifetime)
        cleanupCubeMapTexture = false;
    }
    private Texture environmentMap;
    private PbrConfig pbrConfig;

    @Override
    public Shader getFragmentShader() {
        return FRAGMENT_SHADER;
    }

    @Override
    protected void initPushConstants() {
        super.initPushConstants();
        pushConstants.getData().setFloat("roughness", 0f);
        pushConstants.getData().setInt("numSamples", pbrConfig.getPrefilteredEnvironmentMapSamples());
    }

    @Override
    protected void updatePushConstants(int mipLevel, int face) {
        super.updatePushConstants(mipLevel, face);
        float roughness = (((float) mipLevel) / (cubeMapTexture.getMipLevels() - 1));
        pushConstants.getData().setFloat("roughness", roughness);
    }

    @Override
    protected void initResourceDescriptorSet() {
        super.initResourceDescriptorSet();
        // Descriptor is already needed here
        environmentMap.updateNative(application);
        resourceDescriptorSet.setDescriptor("environmentMap", environmentMap.getDescriptor("default"));
    }
}
