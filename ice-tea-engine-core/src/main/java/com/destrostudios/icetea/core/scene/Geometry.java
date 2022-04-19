package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.collision.BoundingBox;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorWithLayout;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.render.GeometryRenderContext;
import com.destrostudios.icetea.core.data.UniformData;
import com.destrostudios.icetea.core.render.RenderJob;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;

public class Geometry extends Spatial {

    public Geometry() {
        transformUniformData = new UniformData();
    }

    public Geometry(Geometry geometry, CloneContext context) {
        super(geometry, context);
        setMesh(context.isCloneMeshes() ? context.cloneByReference(geometry.mesh) : geometry.mesh);
        setMaterial(context.isCloneMaterials() ? context.cloneByReference(geometry.material) : geometry.material);
        transformUniformData = geometry.transformUniformData.clone(context);
    }
    @Getter
    protected Mesh mesh;
    @Getter
    protected Material material;
    @Getter
    private UniformData transformUniformData;
    @Getter
    private HashMap<RenderJob<?>, GeometryRenderContext<?>> renderContexts = new HashMap<>();

    @Override
    public void update(int imageIndex, float tpf) {
        super.update(imageIndex, tpf);
        updateWorldBounds();
        mesh.update(application, imageIndex, tpf);
        if (mesh.isWereBuffersOutdated()) {
            commandBufferOutdated = true;
        }
        material.update(application, imageIndex, tpf);
        if (material.isCommandBufferOutdated()) {
            commandBufferOutdated = true;
        }
        Set<GeometryRenderContext<?>> outdatedRenderContexts = new HashSet<>();
        if (transformUniformData.updateBufferAndCheckRecreation(application, imageIndex, tpf, application.getSwapChain().getImages().size())) {
            outdatedRenderContexts.addAll(renderContexts.values());
        }
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
                commandBufferOutdated = true;
            }
        });
        if (outdatedRenderContexts.size() > 0) {
            for (GeometryRenderContext<?> renderContext : outdatedRenderContexts) {
                renderContext.recreateDescriptorDependencies();
            }
            commandBufferOutdated = true;
        }
    }

    @Override
    public void updateWorldTransform() {
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

    public GeometryRenderContext<?> getRenderContext(RenderJob<?> renderJob) {
        return renderContexts.get(renderJob);
    }

    public List<MaterialDescriptorWithLayout> getAdditionalMaterialDescriptors() {
        return getAdditionalMaterialDescriptors(this);
    }

    @Override
    protected void collideStatic(Ray ray, Matrix4f worldMatrix, float worldBoundsTMin, float worldBoundsTMax, ArrayList<CollisionResult> collisionResults) {
        collide(collisionResults, () -> mesh.collideStatic(ray, worldTransform.getMatrix(), worldBoundsTMin, worldBoundsTMax, collisionResults));
    }

    @Override
    public void collideDynamic(Ray ray, ArrayList<CollisionResult> collisionResults) {
        collide(collisionResults, () -> {
            mesh.collideDynamic(ray, getVertexPositionModifiers(), worldTransform.getMatrix(), collisionResults);
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

    @Override
    protected void cleanupInternal() {
        transformUniformData.cleanup();
        tryUnregisterMesh();
        tryUnregisterMaterial();
        cleanupRenderContexts();
        super.cleanupInternal();
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

    @Override
    public Geometry clone(CloneContext context) {
        return new Geometry(this, context);
    }
}
