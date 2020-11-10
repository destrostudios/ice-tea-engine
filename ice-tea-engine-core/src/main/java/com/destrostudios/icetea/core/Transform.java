package com.destrostudios.icetea.core;

import lombok.Getter;
import org.joml.*;

@Getter
public class Transform {

    public Transform() {
        translation = new Vector3f();
        rotation = new Quaternionf();
        scale = new Vector3f(1, 1, 1);
        matrix = new Matrix4f();
    }
    @Getter
    private Vector3f translation;
    @Getter
    private Quaternionf rotation;
    @Getter
    private Vector3f scale;
    @Getter
    private Matrix4f matrix;
    private boolean isMatrixOutdated;

    public void set(Transform transform) {
        this.translation.set(transform.getTranslation());
        this.rotation.set(transform.getRotation());
        this.scale.set(transform.getScale());
        this.matrix.set(transform.getMatrix());
    }

    public void setTranslation(Vector3fc translation) {
        this.translation.set(translation);
        isMatrixOutdated = true;
    }

    public void setRotation(Quaternionfc rotation) {
        this.rotation.set(rotation);
        isMatrixOutdated = true;
    }

    public void setScale(Vector3fc scale) {
        this.scale.set(scale);
        isMatrixOutdated = true;
    }

    public void move(Vector3fc translation) {
        this.translation.add(translation);
        isMatrixOutdated = true;
    }

    public void rotate(Quaternionfc rotation) {
        this.rotation.mul(rotation);
        isMatrixOutdated = true;
    }

    public void scale(Vector3fc scale) {
        this.scale.mul(scale);
        isMatrixOutdated = true;
    }

    public boolean updateMatrixIfNecessary() {
        if (isMatrixOutdated) {
            matrix.identity();
            matrix.translate(translation);
            matrix.rotate(rotation);
            matrix.scale(scale);
            isMatrixOutdated = false;
            return true;
        }
        return false;
    }
}
