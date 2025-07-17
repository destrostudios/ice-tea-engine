package com.destrostudios.icetea.core.pbr;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.asset.loader.BufferedTextureLoaderSettings;
import com.destrostudios.icetea.core.compute.ComputeJob;
import com.destrostudios.icetea.core.pbr.compute.PbrBrfdComputeJob;
import com.destrostudios.icetea.core.pbr.render.PbrEnvironmentMapRenderJob;
import com.destrostudios.icetea.core.pbr.render.PbrIrradianceMapRenderJob;
import com.destrostudios.icetea.core.pbr.render.PbrPrefilteredEnvironmentMapRenderJob;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.texture.BufferedTexture;
import com.destrostudios.icetea.core.texture.Texture;
import lombok.AllArgsConstructor;

import static org.lwjgl.vulkan.VK10.VK_FORMAT_R32G32B32A32_SFLOAT;

@AllArgsConstructor
public class PbrManager {

    private Application application;

    public PbrEnvironment createEnvironmentByHdrMap(String equirectangularEnvironmentMapPath, PbrConfig pbrConfig) {
        BufferedTexture equirectangularEnvironmentMap = application.getAssetManager().loadTexture(equirectangularEnvironmentMapPath, BufferedTextureLoaderSettings.builder()
                .format(VK_FORMAT_R32G32B32A32_SFLOAT)
                .flipY(true)
                .build());
        return createEnvironmentByHdrMap(equirectangularEnvironmentMap, pbrConfig);
    }

    public PbrEnvironment createEnvironmentByHdrMap(Texture equirectangularEnvironmentMap, PbrConfig pbrConfig) {
        // Environment map
        PbrEnvironmentMapRenderJob environmentMapRenderJob = new PbrEnvironmentMapRenderJob(equirectangularEnvironmentMap, pbrConfig);
        renderOneTimeAndCleanup(environmentMapRenderJob);
        Texture environmentMap = environmentMapRenderJob.getCubeMapTexture();

        return createEnvironmentByCubeMap(environmentMap, pbrConfig);
    }

    public PbrEnvironment createEnvironmentByCubeMap(Texture environmentMap, PbrConfig pbrConfig) {
        // Irradiance map
        PbrIrradianceMapRenderJob irradianceMapRenderJob = new PbrIrradianceMapRenderJob(environmentMap, pbrConfig);
        renderOneTimeAndCleanup(irradianceMapRenderJob);
        Texture irradianceMap = irradianceMapRenderJob.getCubeMapTexture();

        // Prefiltered environment map
        PbrPrefilteredEnvironmentMapRenderJob prefilteredEnvironmentMapRenderJob = new PbrPrefilteredEnvironmentMapRenderJob(environmentMap, pbrConfig);
        renderOneTimeAndCleanup(prefilteredEnvironmentMapRenderJob);
        Texture prefilteredEnvironmentMap = prefilteredEnvironmentMapRenderJob.getCubeMapTexture();

        // BRDF lookup texture
        PbrBrfdComputeJob brfdComputeJob = new PbrBrfdComputeJob(pbrConfig);
        computeOneTimeAndCleanup(brfdComputeJob);
        Texture brdfLookupTexture = brfdComputeJob.getBrdfLookupTexture();

        return new PbrEnvironment(environmentMap, irradianceMap, prefilteredEnvironmentMap, brdfLookupTexture, pbrConfig.isApplyToneMapping());
    }

    private void renderOneTimeAndCleanup(RenderJob<?> renderJob) {
        // TODO: Only needed because it can be called in Application.init which is called before ShaderManager.application is set (as it has the NativeObject lifecycle)
        application.getShaderManager().updateNative(application);
        application.getSwapChain().getRenderJobManager().renderOneTimeJob(renderJob);
        renderJob.cleanupNative();
    }

    private void computeOneTimeAndCleanup(ComputeJob computeJob) {
        computeJob.updateNative(application);
        computeJob.submit();
        computeJob.cleanupNative();
    }
}
