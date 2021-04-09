package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.material.descriptor.*;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.render.RenderJob;
import lombok.Getter;
import org.joml.Matrix4f;

import java.util.*;

public class Geometry extends Spatial {

    public Geometry() {
        transformUniformData = new UniformData();
        additionalMaterialDescriptors = new LinkedList<>();
        renderContexts = new HashMap<>();
    }
    @Getter
    private Mesh mesh;
    @Getter
    private Material material;
    @Getter
    private UniformData transformUniformData;
    @Getter
    private LinkedList<MaterialDescriptorWithLayout> additionalMaterialDescriptors;
    @Getter
    private HashMap<RenderJob<?>, GeometryRenderContext<?>> renderContexts;

    @Override
    public boolean update(Application application, float tpf) {
        boolean commandBufferOutdated = super.update(application, tpf);
        updateWorldBoundsIfNecessary();

        Set<GeometryRenderContext<?>> outdatedRenderContexts = new HashSet<>();
        application.getSwapChain().getRenderJobManager().forEachRenderJob(renderJob -> {
            if (renderJob.isRendering(this) && (!renderContexts.containsKey(renderJob))) {
                GeometryRenderContext renderContext = renderJob.createGeometryRenderContext();
                renderContext.init(application, renderJob, this);
                renderContexts.put(renderJob, renderContext);
                outdatedRenderContexts.add(renderContext);
            }
        });
        if (transformUniformData.recreateBuffersIfNecessary(application.getSwapChain().getImages().size()) | material.getParameters().recreateBuffersIfNecessary(application.getSwapChain().getImages().size())) {
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

    @Override
    protected void updateWorldBounds() {
        worldBounds.set(mesh.getBounds());
        worldBounds.transform(worldTransform);
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

    public void addAdditionalMaterialDescriptor(MaterialDescriptorWithLayout descriptorWithLayout) {
        additionalMaterialDescriptors.add(descriptorWithLayout);
    }

    public void removeAdditionalMaterialDescriptor(MaterialDescriptorWithLayout descriptorWithLayout) {
        additionalMaterialDescriptors.remove(descriptorWithLayout);
    }

    public GeometryRenderContext<?> getRenderContext(RenderJob<?> renderJob) {
        return renderContexts.get(renderJob);
    }

    @Override
    public void updateUniformBuffers(int currentImage) {
        super.updateUniformBuffers(currentImage);
        transformUniformData.updateBufferIfNecessary(currentImage);
        material.getParameters().updateBufferIfNecessary(currentImage);
    }

    @Override
    protected void collideStatic(Ray ray, Matrix4f worldMatrix, float worldBoundsTMin, float worldBoundsTMax, ArrayList<CollisionResult> collisionResults) {
        collide(collisionResults, () -> mesh.collideStatic(ray, worldTransform.getMatrix(), worldBoundsTMin, worldBoundsTMax, collisionResults));
    }

    @Override
    protected void collideDynamic(Ray ray, Matrix4f worldMatrix, float worldBoundsTMin, float worldBoundsTMax, ArrayList<CollisionResult> collisionResults) {
        collide(collisionResults, () -> mesh.collideDynamic(ray, worldTransform.getMatrix(), collisionResults));
    }

    private void collide(ArrayList<CollisionResult> collisionResults, Runnable addCollisionResults) {
        int previousCollisionsCount = collisionResults.size();
        addCollisionResults.run();
        for (int i = previousCollisionsCount; i < collisionResults.size(); i++) {
            collisionResults.get(i).setGeometry(this);
        }
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
