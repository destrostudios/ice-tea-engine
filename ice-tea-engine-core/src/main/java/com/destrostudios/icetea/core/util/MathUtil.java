package com.destrostudios.icetea.core.util;

import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class MathUtil {

    public static final int UINT32_MAX = 0xFFFFFFFF;
    public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

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
}
