package com.example.kbattery;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/* loaded from: classes3.dex */
public class DirectBufferPool {
    private static native long nativeAlloc(int i);

    private static native void nativeFree(long j, int i);

    static {
        System.loadLibrary("directbuf");
    }

    public static ByteBuffer allocateDirectBuffer(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Size must be positive");
        }
        long nativePtr = nativeAlloc(size);
        if (nativePtr == 0) {
            throw new OutOfMemoryError("Failed to allocate native memory");
        }
        ByteBuffer buffer = ByteBuffer.allocateDirect(size);
        buffer.order(ByteOrder.nativeOrder());
        return buffer;
    }

    public static void freeDirectBuffer(ByteBuffer buffer, int size) {
        if (buffer == null || !buffer.isDirect()) {
            return;
        }
        nativeFree(0L, size);
        buffer.clear();
    }
}
