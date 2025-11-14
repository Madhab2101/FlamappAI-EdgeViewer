#include <jni.h>
#include <vector>
#include "jni_utils.hpp"
#include "opencv_processor.hpp"  // ← Changed from .cpp to .hpp

using namespace flamappai;

extern "C"
JNIEXPORT void JNICALL
Java_com_flamappai_nativebridge_NativeProcessor_nativeInit(
        JNIEnv* env,
        jclass clazz,
        jint width,
        jint height) {
    (void)env; (void)clazz;
    LOGI("nativeInit %dx%d", width, height);
    init(width, height);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_flamappai_nativebridge_NativeProcessor_nativeRelease(
        JNIEnv* env,
        jclass clazz) {
    (void)env; (void)clazz;
    LOGI("nativeRelease");
    release();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_flamappai_nativebridge_NativeProcessor_nativeProcessFrameRgba(
        JNIEnv* env,
        jclass clazz,
        jbyteArray rgbaIn,
        jint width,
        jint height,
        jint mode) {
    (void)clazz;
    jsize len = env->GetArrayLength(rgbaIn);
    if (len < width * height * 4) {
        LOGE("Input buffer too small");
        return;
    }
    jbyte* data = env->GetByteArrayElements(rgbaIn, nullptr);
    processFrame(reinterpret_cast<uint8_t*>(data), mode);
    // Copy processed data back into same array (in-place)
    const auto& out = getOutput();
    env->SetByteArrayRegion(
            rgbaIn, 0, static_cast<jsize>(out.size()),
            reinterpret_cast<const jbyte*>(out.data()));
    env->ReleaseByteArrayElements(rgbaIn, data, 0);
}