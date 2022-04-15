package com.destrostudios.icetea.core.scene;

import com.destrostudios.icetea.core.Application;
import com.destrostudios.icetea.core.Transform;
import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.collision.BoundingBox;
import com.destrostudios.icetea.core.collision.CollisionResult;
import com.destrostudios.icetea.core.collision.CollisionResult_AABB_Ray;
import com.destrostudios.icetea.core.collision.Ray;
import com.destrostudios.icetea.core.light.Light;
import com.destrostudios.icetea.core.material.descriptor.AdditionalMaterialDescriptorProvider;
import com.destrostudios.icetea.core.material.descriptor.MaterialDescriptorWithLayout;
import com.destrostudios.icetea.core.mesh.VertexPositionModifier;
import com.destrostudios.icetea.core.render.bucket.RenderBucketType;
import com.destrostudios.icetea.core.render.shadow.ShadowMode;
import lombok.Getter;
import lombok.Setter;
import org.joml.*;

import java.util.*;
import java.util.function.Predicate;

public abstract class Spatial implements ContextCloneable {

    protected Spatial() {
        localTransform = new Transform();
        worldTransform = new Transform();
        worldBounds = new BoundingBox();
        shadowReceiveWorldBounds = new BoundingBox();
    }

    protected Spatial(Spatial spatial, CloneContext context) {
        // Set parent-child relationships afterwards to avoid circular cloning
        localTransform = spatial.localTransform.clone(context);
        worldTransform = spatial.worldTransform.clone(context);
        worldBounds = spatial.worldBounds.clone(context);
        isWorldTransformOutdated = spatial.isWorldTransformOutdated;
        isWorldBoundsOutdated = spatial.isWorldBoundsOutdated;
        shadowMode = spatial.shadowMode;
        shadowReceiveWorldBounds = spatial.shadowReceiveWorldBounds.clone(context);
        isShadowReceiveWorldBoundsOutdated = spatial.isShadowReceiveWorldBoundsOutdated;
        for (Control control : spatial.controls) {
            controls.add(control.clone(context));
        }
        renderBucket = spatial.renderBucket;
    }
    protected Application application;
    @Getter
    private Node parent;
    @Getter
    protected Transform localTransform;
    @Getter
    protected Transform worldTransform;
    @Getter
    protected BoundingBox worldBounds;
    private boolean isWorldTransformOutdated;
    private boolean isWorldBoundsOutdated;
    @Getter
    private ShadowMode shadowMode = ShadowMode.INHERIT;
    @Getter
    private BoundingBox shadowReceiveWorldBounds;
    private boolean isShadowReceiveWorldBoundsOutdated;
    @Getter
    protected Set<Control> controls = new HashSet<>();
    @Setter
    @Getter
    private RenderBucketType renderBucket;
    // TODO: Introduce TempVars
    private LinkedList<MaterialDescriptorWithLayout> tmpAdditionalMaterialDescriptors = new LinkedList<>();
    private LinkedList<VertexPositionModifier> tmpVertexPositionModifiers = new LinkedList<>();

    public boolean update(Application application, float tpf) {
        if (this.application == null) {
            this.application = application;
            init();
        }
        ensureControlsState();
        for (Control control : controls) {
            control.update(tpf);
        }
        if (localTransform.updateMatrixIfNecessary()) {
            setWorldTransformOutdated();
            setWorldBoundsOutdated();
        }
        updateWorldTransformIfNecessary();
        return false;
    }

    protected void init() {
        setWorldTransformOutdated();
        setWorldBoundsOutdated();
        ensureControlsState();
    }

    protected void setWorldTransformOutdated() {
        isWorldTransformOutdated = true;
    }

    protected void setWorldBoundsOutdated() {
        isWorldBoundsOutdated = true;
        if (parent != null) {
            parent.setWorldBoundsOutdated();
        }
        setShadowReceiveWorldBoundsOutdated();
    }

    private void ensureControlsState() {
        for (Control control : controls) {
            if (!control.isInitialized()) {
                control.init(application);
            }
            control.setSpatial(this);
        }
    }

    protected void updateWorldTransformIfNecessary() {
        if (isWorldTransformOutdated) {
            updateWorldTransform();
            isWorldTransformOutdated = false;
        }
    }

    public void updateWorldTransform() {
        if (parent != null) {
            worldTransform.setChildWorldTransform(parent.getWorldTransform(), localTransform);
            worldTransform.updateMatrixIfNecessary();
        } else {
            worldTransform.set(localTransform);
        }
    }

    protected void updateWorldBoundsIfNecessary() {
        if (isWorldBoundsOutdated) {
            updateWorldBounds(worldBounds, spatial -> true);
            isWorldBoundsOutdated = false;
        }
    }

    protected abstract void updateWorldBounds(BoundingBox destinationWorldBounds, Predicate<Spatial> isSpatialConsidered);

    public void setLocalTransform(Transform transform) {
        localTransform.set(transform);
    }

    public void setLocalTransform(Matrix4fc transform) {
        localTransform.set(transform);
    }

    public void setLocalTranslation(Vector3fc translation) {
        localTransform.setTranslation(translation);
    }

    public void setLocalRotation(Quaternionfc rotation) {
        localTransform.setRotation(rotation);
    }

    public void setLocalScale(Vector3fc scale) {
        localTransform.setScale(scale);
    }

    public void move(Vector3fc translation) {
        localTransform.move(translation);
    }

    public void rotate(Quaternionfc rotation) {
        localTransform.rotate(rotation);
    }

    public void scale(Vector3fc scale) {
        localTransform.scale(scale);
    }

    public List<Light> getAffectingLights() {
        List<Light> affectingLights = new LinkedList<>();
        Light light = application.getLight();
        if ((light != null) && light.isAffecting(this)) {
            affectingLights.add(light);
        }
        return affectingLights;
    }

    public void updateUniformBuffers(int currentImage) {
        for (Control control : controls) {
            control.updateUniformBuffers(currentImage);
        }
    }

    public void collideStatic(Ray ray, ArrayList<CollisionResult> collisionResults) {
        CollisionResult_AABB_Ray worldBoundCollision = worldBounds.collide(ray);
        if (worldBoundCollision != null) {
           collideStatic(ray, worldTransform.getMatrix(), worldBoundCollision.getTMin(), worldBoundCollision.getTMax(), collisionResults);
        }
    }

    protected abstract void collideStatic(Ray ray, Matrix4f worldMatrix, float worldBoundsTMin, float worldBoundsTMax, ArrayList<CollisionResult> collisionResults);

    public abstract void collideDynamic(Ray ray, ArrayList<CollisionResult> collisionResults);

    // TODO: Changing shadow mode on parent nodes should outdate the children - Before implementing, decide if to continue with the INHERIT approach
    public void setShadowMode(ShadowMode shadowMode) {
        boolean wasReceivingShadows = isReceivingShadows();
        this.shadowMode = shadowMode;
        boolean isReceivingShadows = isReceivingShadows();
        if (isReceivingShadows != wasReceivingShadows) {
            setShadowReceiveWorldBoundsOutdated();
        }
    }

    protected void setShadowReceiveWorldBoundsOutdated() {
        isShadowReceiveWorldBoundsOutdated = true;
        if (parent != null) {
            parent.setShadowReceiveWorldBoundsOutdated();
        }
    }

    protected boolean updateShadowReceiveWorldBoundsIfNecessary() {
        if (isShadowReceiveWorldBoundsOutdated) {
            updateWorldBounds(shadowReceiveWorldBounds, Spatial::isReceivingShadows);
            isShadowReceiveWorldBoundsOutdated = false;
            return true;
        }
        return false;
    }

    public boolean isReceivingShadows() {
        return (shadowMode == ShadowMode.RECEIVE)
            || (shadowMode == ShadowMode.CAST_AND_RECEIVE)
            || ((shadowMode == ShadowMode.INHERIT) && ((parent != null) && parent.isReceivingShadows()));
    }

    public boolean isCastingShadows() {
        return (shadowMode == ShadowMode.CAST)
            || (shadowMode == ShadowMode.CAST_AND_RECEIVE)
            || ((shadowMode == ShadowMode.INHERIT) && ((parent != null) && parent.isCastingShadows()));
    }

    public void addControl(Control control) {
        controls.add(control);
    }

    public void removeControl(Control control) {
        if (controls.remove(control)) {
            if (isAttachedToRoot()) {
                control.onRemoveFromRoot();
            }
            control.onRemove();
        }
    }

    public <T extends Control> T getFirstControl(Class<T> controlClass) {
        for (Control control : controls) {
            if (controlClass.isAssignableFrom(control.getClass())) {
                return (T) control;
            }
        }
        return null;
    }

    public void removeFromParent() {
        if (parent != null) {
            parent.remove(this);
        }
    }

    public void setParent(Node parent) {
        if ((parent == null) && isAttachedToRoot()) {
            onRemoveFromRoot();
        }
        this.parent = parent;
    }

    public boolean isAttachedToRoot() {
        return (application != null) && hasParent(application.getRootNode());
    }

    public boolean hasParent(Spatial spatial) {
        Spatial currentSpatial = parent;
        while (currentSpatial != null) {
            if (currentSpatial == spatial) {
                return true;
            }
            currentSpatial = currentSpatial.getParent();
        }
        return false;
    }

    protected void onRemoveFromRoot() {
        for (Control control : controls) {
            control.onRemoveFromRoot();
        }
    }

    protected List<MaterialDescriptorWithLayout> getAdditionalMaterialDescriptors(Geometry geometry) {
        tmpAdditionalMaterialDescriptors.clear();
        if (parent != null) {
            tmpAdditionalMaterialDescriptors.addAll(parent.getAdditionalMaterialDescriptors(geometry));
        }
        for (Control control : controls) {
            if (control instanceof AdditionalMaterialDescriptorProvider) {
                AdditionalMaterialDescriptorProvider additionalMaterialDescriptorProvider = (AdditionalMaterialDescriptorProvider) control;
                tmpAdditionalMaterialDescriptors.addAll(additionalMaterialDescriptorProvider.getAdditionalMaterialDescriptors(geometry));
            }
        }
        return tmpAdditionalMaterialDescriptors;
    }

    protected List<VertexPositionModifier> getVertexPositionModifiers() {
        tmpVertexPositionModifiers.clear();
        if (parent != null) {
            tmpVertexPositionModifiers.addAll(parent.getVertexPositionModifiers());
        }
        for (Control control : controls) {
            if (control instanceof VertexPositionModifier) {
                VertexPositionModifier vertexPositionModifier = (VertexPositionModifier) control;
                tmpVertexPositionModifiers.add(vertexPositionModifier);
            }
        }
        return tmpVertexPositionModifiers;
    }

    @Override
    public abstract Spatial clone(CloneContext context);
}
