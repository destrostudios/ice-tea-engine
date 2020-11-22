package com.destrostudios.icetea.core;

import java.io.IOException;
import java.io.InputStream;

public class LowEndianUtil {

    public static int readUnsignedShort(InputStream inputStream) throws IOException {
        int ch1 = inputStream.read();
        int ch2 = inputStream.read();
        return ((ch2 << 8) + ch1);
    }

    public static float readFloat(InputStream inputStream) throws IOException {
        int ch1 = inputStream.read();
        int ch2 = inputStream.read();
        int ch3 = inputStream.read();
        int ch4 = inputStream.read();
        return Float.intBitsToFloat((ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1);
    }
}
