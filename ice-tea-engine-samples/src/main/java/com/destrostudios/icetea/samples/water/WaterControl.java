package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.render.RenderJobManager;
import com.destrostudios.icetea.core.scene.Control;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;

import static org.lwjgl.vulkan.VK10.*;

public class WaterControl extends Control {

    public WaterControl(WaterConfig waterConfig) {
        this.waterConfig = waterConfig;
        twiddleFactorsComputeJob = new TwiddleFactorsComputeJob(waterConfig.getN());
        h0kComputeJob = new H0kComputeJob(waterConfig);
        hktComputeJob = new HktComputeJob(waterConfig, h0kComputeJob);
        fftComputeJob = new FftComputeJob(waterConfig.getN(), twiddleFactorsComputeJob, hktComputeJob);
        normalMapComputeJob = new NormalMapComputeJob(waterConfig, fftComputeJob);
    }
    private WaterConfig waterConfig;
    private TwiddleFactorsComputeJob twiddleFactorsComputeJob;
    private H0kComputeJob h0kComputeJob;
    private HktComputeJob hktComputeJob;
    private FftComputeJob fftComputeJob;
    private NormalMapComputeJob normalMapComputeJob;
    private ReflectionRenderJob reflectionRenderJob;
    private RefractionRenderJob refractionRenderJob;
    private float time;
    private float motion;
    private float distortion;
    private boolean computedStaticTextures;

    @Override
    protected void onAttached() {
        super.onAttached();
        Geometry geometry = (Geometry) spatial;

        RenderJobManager renderJobManager = application.getSwapChain().getRenderJobManager();
        reflectionRenderJob = new ReflectionRenderJob(geometry);
        refractionRenderJob = new RefractionRenderJob(geometry);
        renderJobManager.addPreSceneRenderJob(reflectionRenderJob);
        renderJobManager.addPreSceneRenderJob(refractionRenderJob);

        // The material immediately needs the textures (before SwapChain.update)
        reflectionRenderJob.updateNative(application);
        refractionRenderJob.updateNative(application);
        geometry.setMaterial(createMaterial());
    }

    private Material createMaterial() {
        Material material = new Material();
        material.setVertexShader(new Shader("com/destrostudios/icetea/samples/shaders/water/water.vert"));
        material.setFragmentShader(new Shader("com/destrostudios/icetea/samples/shaders/water/water.frag"));
        material.setTessellationPatchSize(16);
        material.setTessellationControlShader(new Shader("com/destrostudios/icetea/samples/shaders/water/water.tesc"));
        material.setTessellationEvaluationShader(new Shader("com/destrostudios/icetea/samples/shaders/water/water.tese"));
        material.setGeometryShader(new Shader("com/destrostudios/icetea/samples/shaders/water/water.geom"));
        // FIXME: Vectors have to be defined first or somehow the memory alignment is messed up
        material.getParameters().setVector3f("waterColor", waterConfig.getWaterColor());
        material.getParameters().setVector2f("windDirection", waterConfig.getWindDirection());
        // Tessellation
        material.getParameters().setFloat("tessellationFactor", waterConfig.getTessellationFactor());
        material.getParameters().setFloat("tessellationSlope", waterConfig.getTessellationSlope());
        material.getParameters().setFloat("tessellationShift", waterConfig.getTessellationShift());
        material.getParameters().setFloat("uvScale", waterConfig.getUvScale());
        // Geometry
        material.getParameters().setFloat("displacementRange", waterConfig.getDisplacementRange());
        material.getParameters().setFloat("displacementRangeSurrounding", waterConfig.getDisplacementRangeSurrounding());
        material.getParameters().setFloat("highDetailRangeGeometry", waterConfig.getHighDetailRangeGeometry());
        material.setTexture("dxMap", fftComputeJob.getDxTexture());
        material.setTexture("dyMap", fftComputeJob.getDyTexture());
        material.setTexture("dzMap", fftComputeJob.getDzTexture());
        material.getParameters().setFloat("displacementScale", waterConfig.getDisplacementScale());
        material.getParameters().setFloat("choppiness", waterConfig.getChoppiness());
        // Fragment
        material.getParameters().setFloat("highDetailRangeTexture", waterConfig.getHighDetailRangeTexture());
        material.setTexture("normalMap", normalMapComputeJob.getNormalMapTexture());
        material.getParameters().setFloat("capillarStrength", waterConfig.getCapillarStrength());
        material.getParameters().setFloat("capillarDownsampling", waterConfig.getCapillarDownsampling());
        material.setTexture("dudvMap", application.getAssetManager().loadTexture(waterConfig.getDudvMapFilePath()));
        material.getParameters().setFloat("dudvDownsampling", waterConfig.getDudvDownsampling());
        material.setTexture("reflectionMap", reflectionRenderJob.getResolvedColorTexture());
        material.setTexture("refractionMap", refractionRenderJob.getResolvedColorTexture());
        material.getParameters().setFloat("kReflection", waterConfig.getKReflection());
        material.getParameters().setFloat("kRefraction", waterConfig.getKRefraction());
        material.getParameters().setFloat("reflectionBlendMinFactor", waterConfig.getReflectionBlendMinFactor());
        material.getParameters().setFloat("reflectionBlendMaxFactor", waterConfig.getReflectionBlendMaxFactor());
        material.getParameters().setFloat("reflectionBlendMaxDistance", waterConfig.getReflectionBlendMaxDistance());
        material.getParameters().setFloat("eta", waterConfig.getEta());
        material.getParameters().setFloat("fresnelFactor", waterConfig.getFresnelFactor());
        // Movement
        material.getParameters().setFloat("motion", motion);
        material.getParameters().setFloat("distortion", distortion);
        return material;
    }

    @Override
    public void updateLogicalState(Application application, float tpf) {
        super.updateLogicalState(application, tpf);
        time += tpf * waterConfig.getTimeSpeed();
        motion += tpf * waterConfig.getMotionSpeed();
        distortion += tpf * waterConfig.getDistortionSpeed();

        hktComputeJob.setTime(time);

        Geometry geometry = (Geometry) spatial;
        geometry.getMaterial().getParameters().setFloat("motion", motion);
        geometry.getMaterial().getParameters().setFloat("distortion", distortion);
    }

    @Override
    public void updateNativeState(Application application) {
        super.updateNativeState(application);

        // Static textures

        twiddleFactorsComputeJob.updateNative(application);
        h0kComputeJob.updateNative(application);

        if (!computedStaticTextures) {
            twiddleFactorsComputeJob.submit();
            h0kComputeJob.submit();
            computedStaticTextures = true;
        }

        // Dynamic textures

        hktComputeJob.updateNative(application);
        hktComputeJob.submit();

        fftComputeJob.updateNative(application);
        fftComputeJob.setWait(hktComputeJob.getSignalSemaphore(), VK_PIPELINE_STAGE_ALL_COMMANDS_BIT);
        fftComputeJob.submit();

        normalMapComputeJob.updateNative(application);
        normalMapComputeJob.setWait(fftComputeJob.getSignalSemaphore(), VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
        normalMapComputeJob.submit();
    }

    @Override
    public void onDetached() {
        super.onDetached();
        RenderJobManager renderJobManager = application.getSwapChain().getRenderJobManager();
        renderJobManager.removePostSceneRenderJob(reflectionRenderJob);
        renderJobManager.removePostSceneRenderJob(refractionRenderJob);
        cleanupRenderJobs();
    }

    @Override
    public void cleanupNativeStateInternal() {
        twiddleFactorsComputeJob.cleanupNative();
        h0kComputeJob.cleanupNative();
        hktComputeJob.cleanupNative();
        fftComputeJob.cleanupNative();
        normalMapComputeJob.cleanupNative();
        computedStaticTextures = false;
        cleanupRenderJobs();
        super.cleanupNativeStateInternal();
    }

    private void cleanupRenderJobs() {
        reflectionRenderJob.cleanupNative();
        refractionRenderJob.cleanupNative();
    }

    @Override
    public WaterControl clone(CloneContext context) {
        throw new UnsupportedOperationException();
    }
}
