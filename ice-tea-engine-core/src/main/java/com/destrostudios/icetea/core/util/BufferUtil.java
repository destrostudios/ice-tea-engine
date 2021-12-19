package com.destrostudios.icetea.core.util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.List;

public class BufferUtil {

    public static PointerBuffer asPointerBuffer(Collection<String> collection, MemoryStack stack) {
        PointerBuffer buffer = stack.mallocPointer(collection.size());
        collection.stream()
                .map(stack::UTF8)
                .forEach(buffer::put);
        return buffer.rewind();
    }

    public static PointerBuffer asPointerBuffer(List<? extends Pointer> pointerList, MemoryStack stack) {
        PointerBuffer buffer = stack.mallocPointer(pointerList.size());
        pointerList.forEach(buffer::put);
        return buffer.rewind();
    }

    public static void memcpy(ByteBuffer source, ByteBuffer destination, long size) {
        source.limit((int) size);
        destination.put(source);
        source.limit(source.capacity()).rewind();
    }

    public static void memcpy(ByteBuffer buffer, int[] indices) {
        for (int index : indices) {
            buffer.putInt(index);
        }
        buffer.rewind();
    }
}
