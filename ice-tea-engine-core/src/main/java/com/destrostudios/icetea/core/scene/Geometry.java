package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.collision.BoundingBox;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.resource.descriptor.GeometryTransformDescriptor;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;

public class Geometry extends Spatial {

    public Geometry() {
        transformUniformBuffer = new UniformDataBuffer();
        transformUniformBuffer.setDescriptor("default", new GeometryTransformDescriptor());
    }

    public Geometry(Geometry geometry, CloneContext context) {
        super(geometry, context);
        setMesh(context.isCloneMeshes() ? context.cloneByReference(geometry.mesh) : geometry.mesh);
        setMaterial(context.isCloneMaterials() ? context.cloneByReference(geometry.material) : geometry.material);
        transformUniformBuffer = geometry.transformUniformBuffer.clone(context);
    }
    @Getter
    protected Mesh mesh;
    @Getter
    protected Material material;
    @Getter
    private UniformDataBuffer transformUniformBuffer;

    @Override
    public void update(float tpf) {
        super.update(tpf);
        updateWorldBounds();
        mesh.update(application, tpf);
        material.update(application, tpf);
        application.getSwapChain().setResourceActive(transformUniformBuffer);
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
        transformUniformBuffer.getData().setMatrix4f("model", worldTransform.getMatrix());
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

    public void addAdditionalResourceDescriptors(Map<String, ResourceDescriptor<?>> resourceDescriptors) {
        super.addAdditionalResourceDescriptors(this, resourceDescriptors);
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
    protected void cleanupInternal() {
        transformUniformBuffer.cleanup();
        tryUnregisterMesh();
        tryUnregisterMaterial();
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

    @Override
    public Geometry clone(CloneContext context) {
        return new Geometry(this, context);
    }
}
