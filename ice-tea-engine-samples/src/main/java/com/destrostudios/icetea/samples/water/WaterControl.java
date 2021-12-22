package com.destrostudios.icetea.samples.water;

import com.destrostudios.icetea.core.*;
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
    public void init(Application application) {
        super.init(application);
        twiddleFactorsComputeJob = new TwiddleFactorsComputeJob(waterConfig.getN());
        twiddleFactorsComputeJob.init(application);
        twiddleFactorsComputeJob.submit();

        h0kComputeJob = new H0kComputeJob(waterConfig);
        h0kComputeJob.init(application);
        h0kComputeJob.submit();

        hktComputeJob = new HktComputeJob(waterConfig, h0kComputeJob.getH0kTexture(), h0kComputeJob.getH0minuskTexture());
        hktComputeJob.init(application);
        hktComputeJob.submit();

        fftComputeJob = new FftComputeJob(waterConfig.getN(), twiddleFactorsComputeJob.getTwiddleFactorsTexture(), hktComputeJob.getDxCoefficientsTexture(), hktComputeJob.getDyCoefficientsTexture(), hktComputeJob.getDzCoefficientsTexture());
        fftComputeJob.setWait(hktComputeJob.getSignalSemaphore(), VK_PIPELINE_STAGE_ALL_COMMANDS_BIT);
        fftComputeJob.init(application);
        fftComputeJob.submit();

        normalMapComputeJob = new NormalMapComputeJob(waterConfig, fftComputeJob.getDyTexture());
        normalMapComputeJob.setWait(fftComputeJob.getSignalSemaphore(), VK_PIPELINE_STAGE_COMPUTE_SHADER_BIT);
        normalMapComputeJob.init(application);
        normalMapComputeJob.submit();
    }

    @Override
    protected void onAdd() {
        super.onAdd();
        Geometry geometry = (Geometry) spatial;
        geometry.setMaterial(createMaterial());

        cleanupRenderJobs();
        reflectionRenderJob = new ReflectionRenderJob(geometry);
        refractionRenderJob = new RefractionRenderJob(geometry);
    }

    private Material createMaterial() {
        Material material = new Material();
        material.setVertexShader(new Shader("shaders/water/water.vert"));
        material.setFragmentShader(new Shader("shaders/water/water.frag"));
        material.setTessellationPatchSize(16);
        material.setTessellationControlShader(new Shader("shaders/water/water.tesc"));
        material.setTessellationEvaluationShader(new Shader("shaders/water/water.tese"));
        material.setGeometryShader(new Shader("shaders/water/water.geom"));
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
        material.setTexture("reflectionMap", () -> reflectionRenderJob.getResolvedColorTexture());
        material.setTexture("refractionMap", () -> refractionRenderJob.getResolvedColorTexture());
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
    public void update(float tpf) {
        super.update(tpf);
        time += tpf * waterConfig.getTimeSpeed();
        motion += tpf * waterConfig.getMotionSpeed();
        distortion += tpf * waterConfig.getDistortionSpeed();

        hktComputeJob.setTime(time);
        hktComputeJob.getUniformData().updateBufferIfNecessary(0);
        hktComputeJob.submit();

        fftComputeJob.submit();

        normalMapComputeJob.submit();

        Geometry geometry = (Geometry) spatial;
        geometry.getMaterial().getParameters().setFloat("motion", motion);
        geometry.getMaterial().getParameters().setFloat("distortion", distortion);
    }

    @Override
    protected void onActiveChanged() {
        super.onActiveChanged();
        LinkedList<RenderJob<?>> queuePreScene = application.getSwapChain().getRenderJobManager().getQueuePreScene();
        if (active) {
            queuePreScene.add(reflectionRenderJob);
            queuePreScene.add(refractionRenderJob);
        } else {
            queuePreScene.remove(reflectionRenderJob);
            queuePreScene.remove(refractionRenderJob);
        }
        application.recreateRenderJobs();
    }

    @Override
    public void cleanup() {
        super.cleanup();
        twiddleFactorsComputeJob.cleanup();
        h0kComputeJob.cleanup();
        hktComputeJob.cleanup();
        fftComputeJob.cleanup();
        normalMapComputeJob.cleanup();
        cleanupRenderJobs();
    }

    private void cleanupRenderJobs() {
        if (reflectionRenderJob != null) {
            reflectionRenderJob.cleanup();
            reflectionRenderJob = null;
        }
        if (refractionRenderJob != null) {
            refractionRenderJob.cleanup();
            refractionRenderJob = null;
        }
    }
}
