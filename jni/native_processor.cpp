extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_flamappai_nativebridge_NativeProcessor_processFrameRgba(
        JNIEnv *env,
        jobject /*thiz*/,
        jbyteArray rgbaIn,
        jint width,
        jint height,
        jint mode);

