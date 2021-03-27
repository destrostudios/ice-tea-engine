package com.destrostudios.icetea.core.collision;

import com.destrostudios.icetea.core.Transform;
import com.destrostudios.icetea.core.util.MathUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.joml.Matrix3f;
import org.joml.Vector3f;

@AllArgsConstructor
@Getter
public class BoundingBox {

    public BoundingBox() {
        this(new Vector3f(), new Vector3f());
    }

    public BoundingBox(BoundingBox boundingBox) {
        this(new Vector3f(boundingBox.getCenter()), new Vector3f(boundingBox.getExtent()));
    }
    private Vector3f center;
    private Vector3f extent;

    public void set(BoundingBox boundingBox) {
        center.set(boundingBox.getCenter());
        extent.set(boundingBox.getExtent());
    }

    public void setMin(int axis, float value) {
        setMinMax(getMin().setComponent(axis, value), getMax());
    }

    public void setMin(Vector3f min) {
        setMinMax(min, getMax());
    }

    public void setMax(int axis, float value) {
        setMinMax(getMin(), getMax().setComponent(axis, value));
    }

    public void setMax(Vector3f max) {
        setMinMax(getMin(), max);
    }

    public Vector3f getMin() {
        return center.sub(extent, new Vector3f());
    }

    public Vector3f getMax() {
        return center.add(extent, new Vector3f());
    }

    public void setMinMax(Vector3f min, Vector3f max) {
        center.set(max).add(min).mul(0.5f);
        extent.set(Math.abs(max.x() - center.x()), Math.abs(max.y() - center.y()), Math.abs(max.z() - center.z()));
    }

    public void transform(Transform transform) {
        center.mul(transform.getScale());
        center.rotate(transform.getRotation());
        center.add(transform.getTranslation());

        // TODO: Introduce TempVars
        Vector3f tmp = new Vector3f();
        tmp.set(extent.x() * Math.abs(transform.getScale().x()), extent.y() * Math.abs(transform.getScale().y()), extent.z() * Math.abs(transform.getScale().z()));
        Matrix3f rotationMatrix = new Matrix3f();
        rotationMatrix.set(transform.getRotation());
        // Make the rotation matrix all positive to get the maximum x/y/z extent
        MathUtil.absoluteLocal(rotationMatrix);
        tmp.mul(rotationMatrix);
        // Assign the biggest rotations after scales
        extent.set(Math.abs(tmp.x()), Math.abs(tmp.y()), Math.abs(tmp.z()));
    }

    public CollisionResult_AABB_Ray collide(Ray ray) {
        Vector3f min = getMin();
        Vector3f max = getMax();

        float t1 = (min.x() - ray.getOrigin().x()) / ray.getDirection().x();
        float t2 = (max.x() - ray.getOrigin().x()) / ray.getDirection().x();
        float t3 = (min.y() - ray.getOrigin().y()) / ray.getDirection().y();
        float t4 = (max.y() - ray.getOrigin().y()) / ray.getDirection().y();
        float t5 = (min.z() - ray.getOrigin().z()) / ray.getDirection().z();
        float t6 = (max.z() - ray.getOrigin().z()) / ray.getDirection().z();

        float tMax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));
        if (tMax >= 0) {
            float tMin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
            if (tMin <= tMax) {
                return new CollisionResult_AABB_Ray(Math.max(0, tMin), tMax);
            }
        }
        return null;
    }
}
