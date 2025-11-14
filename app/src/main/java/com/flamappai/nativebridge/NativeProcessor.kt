package com.flamappai.nativebridge

object NativeProcessor {
    init {
        System.loadLibrary("flam_native") // name of native lib from CMake
    }

    external fun processFrameRgba(
        rgba: ByteArray,
        width: Int,
        height: Int,
        mode: Int  // 0=raw,1=gray,2=edge
    ): ByteArray
}
