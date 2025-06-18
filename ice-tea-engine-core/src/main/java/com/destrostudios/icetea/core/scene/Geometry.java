package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.buffer.UniformDataBuffer;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.collision.BoundingBox;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.material.Material;
import com.destrostudios.icetea.core.mesh.Mesh;
import com.destrostudios.icetea.core.render.GeometryRenderer;
import com.destrostudios.icetea.core.resource.ResourceDescriptor;
import com.destrostudios.icetea.core.resource.descriptor.GeometryTransformDescriptor;
import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Map;
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
    private static final GeometryRenderer DEFAULT_GEOMETRY_RENDERER = new GeometryRenderer();
    @Getter
    protected Mesh mesh;
    @Getter
    protected Material material;
    @Getter
    private UniformDataBuffer transformUniformBuffer;
    @Getter
    @Setter
    protected GeometryRenderer renderer = DEFAULT_GEOMETRY_RENDERER;

    @Override
    public void applyLogicalState() {
        super.applyLogicalState();
        updateWorldBounds();
    }

    @Override
    public void updateNativeState(Application application) {
        super.updateNativeState(application);
        mesh.updateNative(application, this);
        material.updateNative(application, this);
        transformUniformBuffer.updateNative(application);
    }

    @Override
    public void updateTransform() {
        super.updateTransform();
        transformUniformBuffer.getData().setMatrix4f("model", worldTransform.getMatrix());
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

    public void setMesh(Mesh mesh) {
        if (this.mesh != null) {
            this.mesh.removeConsumer(this);
        }
        mesh.addConsumer(this);
        this.mesh = mesh;
    }

    public void setMaterial(Material material) {
        if (this.material != null) {
            this.material.removeConsumer(this);
        }
        material.addConsumer(this);
        this.material = material;
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
    public void cleanupNativeStateInternal() {
        transformUniformBuffer.cleanupNative();
        if (mesh != null) {
            mesh.onConsumerCleanup(this);
        }
        if (material != null) {
            material.onConsumerCleanup(this);
        }
        super.cleanupNativeStateInternal();
    }

    @Override
    public Geometry clone(CloneContext context) {
        return new Geometry(this, context);
    }
}
