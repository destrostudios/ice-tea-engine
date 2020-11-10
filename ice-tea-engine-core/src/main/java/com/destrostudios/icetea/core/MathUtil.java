package com.destrostudios.icetea.core;

public class MathUtil {

    public static final int UINT32_MAX = 0xFFFFFFFF;
    public static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

    public static int clamp(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    public static double log2(double n) {
        return Math.log(n) / Math.log(2);
    }
}
