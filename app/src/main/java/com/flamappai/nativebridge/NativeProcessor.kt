package com.flamappai.nativebridge

object NativeProcessor {

    init {
        // Order matters: first OpenCV, then our lib
        System.loadLibrary("opencv_java4")
        System.loadLibrary("flam_native")
    }

    @JvmStatic external fun nativeInit(width: Int, height: Int)
    @JvmStatic external fun nativeRelease()

    /**
     * Process RGBA8888 image in-place via OpenCV.
     * @param rgba  byte array (size = width * height * 4)
     * @param width image width
     * @param height image height
     * @param mode 0=raw,1=gray,2=edge
     */
    @JvmStatic external fun nativeProcessFrameRgba(
        rgba: ByteArray,
        width: Int,
        height: Int,
        mode: Int
    )
}
