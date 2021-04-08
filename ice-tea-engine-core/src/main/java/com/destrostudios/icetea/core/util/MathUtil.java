package com.destrostudios.icetea.core.util;

import org.joml.AxisAngle4f;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MathUtil {

    public static final int UINT32_MAX = 0xFFFFFFFF;
    public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
    public static final float EPSILON_FLOAT = 1.1920928955078125E-7f;

    public static int clamp(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    public static double log2(double n) {
        return Math.log(n) / Math.log(2);
    }

    public static void setViewMatrix(Matrix4f viewMatrix, Vector3f translation, Vector3f rotation) {
        viewMatrix.identity();
        viewMatrix.rotate(new AxisAngle4f((float) Math.toRadians(rotation.x()), new Vector3f(1, 0, 0)));
        viewMatrix.rotate(new AxisAngle4f((float) Math.toRadians(rotation.y()), new Vector3f(0, 1, 0)));
        viewMatrix.rotate(new AxisAngle4f((float) Math.toRadians(rotation.z()), new Vector3f(0, 0, 1)));
        viewMatrix.translate(translation.negate(new Vector3f()));
    }

    public static void updateMinMax(Vector3f min, Vector3f max, Vector3f value) {
        if (value.x < min.x) {
            min.x = value.x;
        }
        if (value.x > max.x) {
            max.x = value.x;
        }
        if (value.y < min.y) {
            min.y = value.y;
        }
        if (value.y > max.y) {
            max.y = value.y;
        }
        if (value.z < min.z) {
            min.z = value.z;
        }
        if (value.z > max.z) {
            max.z = value.z;
        }
    }

    public static void absoluteLocal(Matrix3f matrix3f) {
        matrix3f.set(
            Math.abs(matrix3f.m00()), Math.abs(matrix3f.m01()), Math.abs(matrix3f.m02()),
            Math.abs(matrix3f.m10()), Math.abs(matrix3f.m11()), Math.abs(matrix3f.m12()),
            Math.abs(matrix3f.m20()), Math.abs(matrix3f.m21()), Math.abs(matrix3f.m22())
        );
    }

    public static Vector3f mul(Vector3f vector3f, Matrix4f matrix4f) {
        return mul(vector3f, matrix4f, vector3f);
    }

    public static Vector3f mul(Vector3f v, Matrix4f m, Vector3f dest) {
        return dest.set(
            (m.m00() * v.x()) + (m.m10() * v.y()) + (m.m20() * v.z()) + m.m30(),
            (m.m01() * v.x()) + (m.m11() * v.y()) + (m.m21() * v.z()) + m.m31(),
            (m.m02() * v.x()) + (m.m12() * v.y()) + (m.m22() * v.z()) + m.m32()
        );
    }

    public static Vector3f mulNormal(Vector3f v, Matrix4f m, Vector3f dest) {
        return dest.set(
            (m.m00() * v.x()) + (m.m10() * v.y()) + (m.m20() * v.z()),
            (m.m01() * v.x()) + (m.m11() * v.y()) + (m.m21() * v.z()),
            (m.m02() * v.x()) + (m.m12() * v.y()) + (m.m22() * v.z())
        );
    }

    public static float mulW(Vector3f v, Matrix4f m) {
        return (m.m03() * v.x()) + (m.m13() * v.y()) + (m.m23() * v.z()) + m.m33();
    }

    public static Vector3f getTriangleNormal(Vector3f v1, Vector3f v2, Vector3f v3) {
        return v2.sub(v1, new Vector3f()).cross(v3.x() - v1.x(), v3.y() - v1.y(), v3.z() - v1.z());
    }
}
