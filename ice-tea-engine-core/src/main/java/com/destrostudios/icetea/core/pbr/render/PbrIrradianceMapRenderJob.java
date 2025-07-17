package com.destrostudios.icetea.core.pbr.render;

import com.destrostudios.icetea.core.pbr.PbrConfig;
import com.destrostudios.icetea.core.render.cubemap.CubeMapRenderJob;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.texture.Texture;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;

public class PbrIrradianceMapRenderJob extends CubeMapRenderJob {

    private static Shader FRAGMENT_SHADER = new FileShader("com/destrostudios/icetea/core/shaders/pbr/irradianceMap.frag");

    public PbrIrradianceMapRenderJob(Texture environmentMap, PbrConfig pbrConfig) {
        this.environmentMap = environmentMap;
        this.pbrConfig = pbrConfig;
        cubeMapConfig.setSize(pbrConfig.getIrradianceMapSize());
        cubeMapConfig.setFormat(VK_FORMAT_R32G32B32A32_SFLOAT);
        cubeMapConfig.setGenerateMipMaps(false);
        // Don't cleanup the cube texture which was passed to the created PbrEnvironment (which owns and controls its lifetime)
        cleanupCubeMapTexture = false;
    }
    private PbrConfig pbrConfig;
    private Texture environmentMap;

    @Override
    public Shader getFragmentShader() {
        return FRAGMENT_SHADER;
    }

    @Override
    protected void initPushConstants() {
        super.initPushConstants();
        pushConstants.getData().setFloat("deltaPhi", (float) ((2 * Math.PI) / pbrConfig.getIrradianceMapSamplesHorizontal()));
        pushConstants.getData().setFloat("deltaTheta", (float) ((Math.PI / 2) / pbrConfig.getIrradianceMapSamplesVertical()));
    }

    @Override
    protected void initResourceDescriptorSet() {
        super.initResourceDescriptorSet();
        // Descriptor is already needed here
        environmentMap.updateNative(application);
        resourceDescriptorSet.setDescriptor("environmentMap", environmentMap.getDescriptor("default"));
    }
}
