#include <jni.h>
#include <android/log.h>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <vector>
#include <cstdint>
#include <memory>

#define LOG_TAG "FlamNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

using namespace cv;

namespace flamappai {

    class Pipeline {
    public:
        Pipeline(int w, int h)
                : width(w), height(h),
                  input(h, w, CV_8UC4),
                  gray(h, w, CV_8UC1),
                  edges(h, w, CV_8UC1),
                  output(h, w, CV_8UC4) {
            buffer.resize(static_cast<size_t>(w) * h * 4);
            LOGI("Pipeline initialized: %dx%d", w, h);
        }

        void process(const uint8_t* rgba, int mode) {
            std::memcpy(input.data, rgba, static_cast<size_t>(width) * height * 4);
            cvtColor(input, gray, COLOR_RGBA2GRAY);

            switch (mode) {
                case 1: // gray
                    cvtColor(gray, output, COLOR_GRAY2RGBA);
                    break;
                case 2: // edges
                    Canny(gray, edges, 100, 200);
                    cvtColor(edges, output, COLOR_GRAY2RGBA);
                    break;
                case 0:
                default:
                    input.copyTo(output);
                    break;
            }

            std::memcpy(buffer.data(), output.data,
                        static_cast<size_t>(width) * height * 4);
        }

        const std::vector<uint8_t>& getBuffer() const { return buffer; }

    private:
        int width;
        int height;
        Mat input, gray, edges, output;
        std::vector<uint8_t> buffer;
    };

    static std::unique_ptr<Pipeline> gPipeline;

} // namespace flamappai

extern "C" {

JNIEXPORT void JNICALL
Java_com_flamappai_nativebridge_NativeProcessor_nativeInit(
        JNIEnv* env,
        jclass clazz,
        jint width,
        jint height) {
    (void)env; (void)clazz;
    LOGI("nativeInit %dx%d", width, height);
    flamappai::gPipeline = std::make_unique<flamappai::Pipeline>(width, height);
}

JNIEXPORT void JNICALL
Java_com_flamappai_nativebridge_NativeProcessor_nativeRelease(
        JNIEnv* env,
        jclass clazz) {
    (void)env; (void)clazz;
    LOGI("nativeRelease");
    flamappai::gPipeline.reset();
}

JNIEXPORT void JNICALL
Java_com_flamappai_nativebridge_NativeProcessor_nativeProcessFrameRgba(
        JNIEnv* env,
        jclass clazz,
        jbyteArray rgbaIn,
        jint width,
        jint height,
        jint mode) {
    (void)clazz;

    if (!flamappai::gPipeline) {
        LOGE("Pipeline not initialized!");
        return;
    }

    jsize len = env->GetArrayLength(rgbaIn);
    if (len < width * height * 4) {
        LOGE("Input buffer too small: %d < %d", len, width * height * 4);
        return;
    }

    jbyte* data = env->GetByteArrayElements(rgbaIn, nullptr);
    if (!data) {
        LOGE("Failed to get array elements");
        return;
    }

    // Process the frame
    flamappai::gPipeline->process(reinterpret_cast<uint8_t*>(data), mode);

    // Copy processed data back into same array (in-place)
    const auto& out = flamappai::gPipeline->getBuffer();
    env->SetByteArrayRegion(
            rgbaIn, 0, static_cast<jsize>(out.size()),
            reinterpret_cast<const jbyte*>(out.data()));

    env->ReleaseByteArrayElements(rgbaIn, data, 0);
}

} // extern "C"