#ifndef FLAMAPPAI_JNI_UTILS_HPP
#define FLAMAPPAI_JNI_UTILS_HPP

#include <android/log.h>

#define LOG_TAG "FlamNative"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,  LOG_TAG, __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)

#endif
