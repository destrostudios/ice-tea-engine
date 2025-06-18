package com.destrostudios.icetea.core;

import com.destrostudios.icetea.core.clone.CloneContext;
import com.destrostudios.icetea.core.clone.ContextCloneable;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.Getter;
import org.joml.*;

@Getter
public class Transform implements ContextCloneable {

    public Transform() {
        translation = new Vector3f();
        rotation = new Quaternionf();
        scale = new Vector3f(1, 1, 1);
        matrix = new Matrix4f();
    }

    public Transform(Transform transform) {
        translation = new Vector3f(transform.translation);
        rotation = new Quaternionf(transform.rotation);
        scale = new Vector3f(transform.scale);
        matrix = new Matrix4f(transform.matrix);
        modified = transform.modified;
        matrixOutdated = transform.matrixOutdated;
    }
    @Getter
    private Vector3f translation;
    @Getter
    private Quaternionf rotation;
    @Getter
    private Vector3f scale;
    @Getter
    private Matrix4f matrix;
    private boolean modified;
    private boolean matrixOutdated;

    public void set(Transform transform) {
        this.translation.set(transform.getTranslation());
        this.rotation.set(transform.getRotation());
        this.scale.set(transform.getScale());
        this.matrix.set(transform.getMatrix());
        modified = transform.modified;
        matrixOutdated = transform.matrixOutdated;
    }

    public void set(Matrix4fc matrix) {
        this.matrix.set(matrix);
        onMatrixSet();
    }

    public void setMultiplicationResult(Transform transformA, Transform transformB) {
        transformA.getMatrix().mul(transformB.getMatrix(), matrix);
        onMatrixSet();
    }

    private void onMatrixSet() {
        matrix.getTranslation(translation);
        MathUtil.getPureRotation(matrix, rotation);
        MathUtil.getPureScale(matrix, scale);
        modified = true;
        matrixOutdated = false;
    }

    public void setTranslation(Vector3fc translation) {
        this.translation.set(translation);
        modified = true;
        matrixOutdated = true;
    }

    public void setRotation(Quaternionfc rotation) {
        this.rotation.set(rotation);
        modified = true;
        matrixOutdated = true;
    }

    public void setScale(Vector3fc scale) {
        this.scale.set(scale);
        modified = true;
        matrixOutdated = true;
    }

    public void move(Vector3fc translation) {
        this.translation.add(translation);
        modified = true;
        matrixOutdated = true;
    }

    public void rotate(Quaternionfc rotation) {
        this.rotation.mul(rotation);
        modified = true;
        matrixOutdated = true;
    }

    public void scale(Vector3fc scale) {
        this.scale.mul(scale);
        modified = true;
        matrixOutdated = true;
    }

    public void combineWithParent(Transform parentWorldTransform, Transform childLocalTransform) {
        scale.set(childLocalTransform.getScale()).mul(parentWorldTransform.getScale());
        parentWorldTransform.getRotation().mul(childLocalTransform.getRotation(), rotation);
        translation.set(childLocalTransform.getTranslation()).mul(parentWorldTransform.getScale());
        translation.rotate(parentWorldTransform.getRotation()).add(parentWorldTransform.getTranslation());
        modified = true;
        matrixOutdated = true;
    }

    public boolean updateMatrixIfNecessary() {
        if (modified) {
            if (matrixOutdated) {
                matrix.rotation(rotation);
                matrix.setTranslation(translation);
                matrix.scale(scale);
                matrixOutdated = false;
            }
            modified = false;
            return true;
        }
        return false;
    }

    @Override
    public Transform clone(CloneContext context) {
        return new Transform(this);
    }
}
