cmake_minimum_required(VERSION 3.22.1)

project("flamappai")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ===== OpenCV Android SDK path =====
# TODO: Update this path to match your OpenCV installation
set(OPENCV_ANDROID_SDK_ROOT "D:/Android/OpenCV/opencv-4.12.0-android-sdk")
set(OPENCV_NATIVE_DIR "${OPENCV_ANDROID_SDK_ROOT}/sdk/native")

# Include OpenCV headers
include_directories("${OPENCV_NATIVE_DIR}/jni/include")

# Import OpenCV shared library
add_library(opencv_java4 SHARED IMPORTED)
set_target_properties(
        opencv_java4
        PROPERTIES IMPORTED_LOCATION
        "${OPENCV_NATIVE_DIR}/libs/${ANDROID_ABI}/libopencv_java4.so"
)

# ===== Our native library =====
add_library(
        flam_native
        SHARED
        native-lib.cpp
)

# Find Android log library
find_library(
        log-lib
        log
)

# Link everything together
target_link_libraries(
        flam_native
        opencv_java4
        ${log-lib}
        android
        GLESv2
)