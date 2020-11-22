package com.destrostudios.icetea.core;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.util.*;

public class Geometry extends Spatial {

    public Geometry() {
        transformUniformData = new UniformData();
        renderContexts = new HashMap<>();
    }
    @Getter
    private Mesh mesh;
    @Getter
    private Material material;
    @Getter
    private UniformData transformUniformData;
    @Getter
    private HashMap<RenderJob<?>, GeometryRenderContext<?>> renderContexts;

    @Override
    public boolean update(Application application) {
        boolean commandBufferOutdated = super.update(application);
        Set<GeometryRenderContext<?>> outdatedRenderContexts = new HashSet<>();
        application.getSwapChain().getRenderJobManager().forEachRenderJob(renderJob -> {
            if (!renderContexts.containsKey(renderJob)) {
                GeometryRenderContext renderContext = renderJob.createGeometryRenderContext();
                renderContext.init(application, renderJob, this);
                renderContexts.put(renderJob, renderContext);
                outdatedRenderContexts.add(renderContext);
            }
        });
        if (transformUniformData.recreateBufferIfNecessary() | material.getParameters().recreateBufferIfNecessary()) {
            outdatedRenderContexts.addAll(renderContexts.values());
        }
        if (outdatedRenderContexts.size() > 0) {
            for (GeometryRenderContext<?> renderContext : outdatedRenderContexts) {
                renderContext.recreateDescriptorDependencies();
            }
            commandBufferOutdated = true;
        }
        return commandBufferOutdated;
    }

    @Override
    public void init() {
        super.init();
        if (!mesh.isInitialized()) {
            mesh.init(application);
        }
        if (!material.isInitialized()) {
            material.init(application);
        }
        transformUniformData.setApplication(application);
        material.getParameters().setApplication(application);
    }

    @Override
    protected void updateWorldTransform() {
        super.updateWorldTransform();
        updateWorldTransformUniform();
    }

    private void updateWorldTransformUniform() {
        transformUniformData.setMatrix4f("model", worldTransform.getMatrix());
    }

    public void setMesh(Mesh mesh) {
        tryUnregisterMesh();
        this.mesh = mesh;
        mesh.increaseUsingGeometriesCount();
    }

    public void setMaterial(Material material) {
        tryUnregisterMaterial();
        this.material = material;
        material.increaseUsingGeometriesCount();
    }

    public GeometryRenderContext<?> getRenderContext(RenderJob<?> renderJob) {
        return renderContexts.get(renderJob);
    }

    public void updateUniformBuffers(int currentImage, MemoryStack stack) {
        transformUniformData.updateBufferIfNecessary(currentImage, stack);
        material.getParameters().updateBufferIfNecessary(currentImage, stack);
    }

    public void cleanup() {
        tryUnregisterMesh();
        tryUnregisterMaterial();
        for (GeometryRenderContext<?> renderContext : renderContexts.values()) {
            renderContext.cleanup();
        }
    }

    private void tryUnregisterMesh() {
        if (mesh != null) {
            mesh.decreaseUsingGeometriesCount();
            if (mesh.isUnused()) {
                mesh.cleanup();
            }
        }
    }

    private void tryUnregisterMaterial() {
        if (material != null) {
            material.decreaseUsingGeometriesCount();
            if (material.isUnused()) {
                material.cleanup();
            }
        }
    }
}
