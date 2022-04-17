package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.*;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.render.RenderJob;
import com.destrostudios.icetea.core.scene.Control;
import com.destrostudios.icetea.core.scene.Geometry;
import com.destrostudios.icetea.core.shader.Shader;

import java.util.LinkedList;

import static org.lwjgl.vulkan.VK10.*;

public class WaterControl extends Control {

    public WaterControl(WaterConfig waterConfig) {
        this.waterConfig = waterConfig;
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

    @Override
    protected void initControl() {
        super.initControl();
        twiddleFactorsComputeJob = new TwiddleFactorsComputeJob(waterConfig.getN());
        twiddleFactorsComputeJob.update(application, 0, 0);
        twiddleFactorsComputeJob.submit();

        h0kComputeJob = new H0kComputeJob(waterConfig);
        h0kComputeJob.update(application, 0, 0);
        h0kComputeJob.submit();

        hktComputeJob = new HktComputeJob(waterConfig, h0kComputeJob);
        hktComputeJob.update(application, 0, 0);
        hktComputeJob.submit();

        fftComputeJob = new FftComputeJob(waterConfig.getN(), twiddleFactorsComputeJob, hktComputeJob);
        fftComputeJob.setWait(hktComputeJob.getSignalSemaphore(), VK_PIPELINE_STAGE_ALL_COMMANDS_BIT);
        fftComputeJob.update(application, 0, 0);
        fftComputeJob.submit();

        normalMapComputeJob = new NormalMapComputeJob(waterConfig, fftComputeJob);
        normalMapComputeJob.setWait(fftComputeJob.getSignalSemaphore(), VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
        normalMapComputeJob.update(application, 0, 0);
        normalMapComputeJob.submit();
    }

    @Override
    protected void onAdd() {
        super.onAdd();
        Geometry geometry = (Geometry) spatial;

        LinkedList<RenderJob<?>> queuePreScene = application.getSwapChain().getRenderJobManager().getQueuePreScene();
        if (reflectionRenderJob != null) {
            cleanupRenderJobs();
            queuePreScene.remove(reflectionRenderJob);
            queuePreScene.remove(refractionRenderJob);
        }
        reflectionRenderJob = new ReflectionRenderJob(geometry);
        refractionRenderJob = new RefractionRenderJob(geometry);
        queuePreScene.add(reflectionRenderJob);
        queuePreScene.add(refractionRenderJob);
        application.recreateRenderJobs();

        geometry.setMaterial(createMaterial(reflectionRenderJob, refractionRenderJob));
    }

    private Material createMaterial(ReflectionRenderJob currentReflectionRenderJob, RefractionRenderJob currentRefractionRenderJob) {
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
        material.setTexture("reflectionMap", currentReflectionRenderJob::getResolvedColorTexture);
        material.setTexture("refractionMap", currentRefractionRenderJob::getResolvedColorTexture);
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
    public void update(Application application, int imageIndex, float tpf) {
        super.update(application, imageIndex, tpf);
        time += tpf * waterConfig.getTimeSpeed();
        motion += tpf * waterConfig.getMotionSpeed();
        distortion += tpf * waterConfig.getDistortionSpeed();

        hktComputeJob.setTime(time);
        hktComputeJob.update(application, imageIndex, tpf);
        hktComputeJob.submit();

        fftComputeJob.update(application, imageIndex, tpf);
        fftComputeJob.submit();

        normalMapComputeJob.update(application, imageIndex, tpf);
        normalMapComputeJob.submit();

        Geometry geometry = (Geometry) spatial;
        geometry.getMaterial().getParameters().setFloat("motion", motion);
        geometry.getMaterial().getParameters().setFloat("distortion", distortion);
    }

    @Override
    protected void onRemove() {
        super.onRemove();
        LinkedList<RenderJob<?>> queuePreScene = application.getSwapChain().getRenderJobManager().getQueuePreScene();
        queuePreScene.remove(reflectionRenderJob);
        queuePreScene.remove(refractionRenderJob);
        application.recreateRenderJobs();
    }

    @Override
    public void cleanup() {
        twiddleFactorsComputeJob.cleanup();
        h0kComputeJob.cleanup();
        hktComputeJob.cleanup();
        fftComputeJob.cleanup();
        normalMapComputeJob.cleanup();
        cleanupRenderJobs();
        super.cleanup();
    }

    private void cleanupRenderJobs() {
        reflectionRenderJob.cleanup();
        refractionRenderJob.cleanup();
    }

    @Override
    public WaterControl clone(CloneContext context) {
        throw new UnsupportedOperationException();
    }
}
