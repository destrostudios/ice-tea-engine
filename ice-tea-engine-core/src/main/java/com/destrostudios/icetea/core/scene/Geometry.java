package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.collision.BoundingBox;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.material.descriptor.*;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.mesh.VertexPositionModifier;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.render.RenderJob;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        AtomicBoolean commandBufferOutdated = new AtomicBoolean(super.update(application, tpf));
        updateWorldBoundsIfNecessary();
        updateShadowReceiveWorldBoundsIfNecessary();
        Set<GeometryRenderContext<?>> outdatedRenderContexts = new HashSet<>();
        application.getSwapChain().getRenderJobManager().forEachRenderJob(renderJob -> {
            GeometryRenderContext renderContext = renderContexts.get(renderJob);
            if (renderJob.isRendering(this)) {
                if (renderContext == null) {
                    renderContext = renderJob.createGeometryRenderContext();
                    renderContext.init(application, renderJob, this);
                    renderContexts.put(renderJob, renderContext);
                    outdatedRenderContexts.add(renderContext);
                }
            } else if (renderContext != null) {
                renderContext.cleanup();
                renderContexts.remove(renderJob);
                commandBufferOutdated.set(true);
            }
        });
        if (transformUniformData.recreateBuffersIfNecessary(application.getSwapChain().getImages().size()) | material.getParameters().recreateBuffersIfNecessary(application.getSwapChain().getImages().size())) {
            outdatedRenderContexts.addAll(renderContexts.values());
        }
        if (outdatedRenderContexts.size() > 0) {
            for (GeometryRenderContext<?> renderContext : outdatedRenderContexts) {
                renderContext.recreateDescriptorDependencies();
            }
            commandBufferOutdated.set(true);
        }
        return commandBufferOutdated.get();
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
    protected void updateWorldBounds(BoundingBox destinationWorldBounds, Predicate<Spatial> isSpatialConsidered) {
        if (isSpatialConsidered.test(this)) {
            destinationWorldBounds.set(mesh.getBounds());
            destinationWorldBounds.transform(worldTransform);
        } else {
            destinationWorldBounds.setMinMax(new Vector3f(), new Vector3f());
        }
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
    public void collideDynamic(Ray ray, ArrayList<CollisionResult> collisionResults) {
        collide(collisionResults, () -> {
            List<VertexPositionModifier> vertexPositionModifiers = controls.stream()
                    .filter(control -> control instanceof VertexPositionModifier)
                    .map(control -> (VertexPositionModifier) control)
                    .collect(Collectors.toList());
            mesh.collideDynamic(ray, vertexPositionModifiers, worldTransform.getMatrix(), collisionResults);
        });
    }

    private void collide(ArrayList<CollisionResult> collisionResults, Runnable addCollisionResults) {
        int previousCollisionsCount = collisionResults.size();
        addCollisionResults.run();
        for (int i = previousCollisionsCount; i < collisionResults.size(); i++) {
            collisionResults.get(i).setGeometry(this);
        }
    }

    @Override
    protected void onRemoveFromRoot() {
        super.onRemoveFromRoot();
        cleanupRenderContexts();
    }

    public void cleanup() {
        tryUnregisterMesh();
        tryUnregisterMaterial();
        cleanupRenderContexts();
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

    private void cleanupRenderContexts() {
        for (GeometryRenderContext<?> renderContext : renderContexts.values()) {
            renderContext.cleanup();
        }
        renderContexts.clear();
    }
}
