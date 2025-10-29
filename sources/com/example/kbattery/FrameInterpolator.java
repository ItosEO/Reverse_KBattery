package com.example.kbattery;

/* loaded from: classes3.dex */
public class FrameInterpolator {
    public static native int calculateBlockDifferenceNeon(byte[] bArr, byte[] bArr2, int i, int i2, int i3, int i4, int i5, int i6, int i7);

    static {
        System.loadLibrary("directbuf");
    }
}
