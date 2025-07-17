package com.destrostudios.icetea.core.pbr.render;

import com.destrostudios.icetea.core.pbr.PbrConfig;
import com.destrostudios.icetea.core.render.cubemap.CubeMapRenderJob;
import com.destrostudios.icetea.core.shader.FileShader;
import com.destrostudios.icetea.core.shader.Shader;
import com.destrostudios.icetea.core.texture.Texture;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;

public class PbrEnvironmentMapRenderJob extends CubeMapRenderJob {

    private static Shader FRAGMENT_SHADER = new FileShader("com/destrostudios/icetea/core/shaders/equirectangularToCubeMap.frag");

    public PbrEnvironmentMapRenderJob(Texture equirectangularEnvironmentMap, PbrConfig pbrConfig) {
        this.equirectangularEnvironmentMap = equirectangularEnvironmentMap;
        cubeMapConfig.setSize(pbrConfig.getEnvironmentMapSize());
        cubeMapConfig.setFormat(VK_FORMAT_R32G32B32A32_SFLOAT);
        cubeMapConfig.setGenerateMipMaps(pbrConfig.isGenerateEnvironmentMapMipMaps());
        // Don't cleanup the cube texture which was passed to the created PbrEnvironment (which owns and controls its lifetime)
        cleanupCubeMapTexture = false;
    }
    private Texture equirectangularEnvironmentMap;

    @Override
    public Shader getFragmentShader() {
        return FRAGMENT_SHADER;
    }

    @Override
    protected void initResourceDescriptorSet() {
        super.initResourceDescriptorSet();
        // Descriptor is already needed here
        equirectangularEnvironmentMap.updateNative(application);
        resourceDescriptorSet.setDescriptor("equirectangularMap", equirectangularEnvironmentMap.getDescriptor("default"));
    }
}
