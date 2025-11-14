# Minimum CMake version
cmake_minimum_required(VERSION 3.22.1)

# Project name (any name is fine here)
project("flamappai")

# Use C++17
set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# ===== OpenCV Android SDK path =====
# TODO: Make sure this path is correct on your machine
set(OPENCV_ANDROID_SDK_ROOT "D:/Android/OpenCV/opencv-4.12.0-android-sdk")
set(OPENCV_NATIVE_DIR "${OPENCV_ANDROID_SDK_ROOT}/sdk/native")

# Include OpenCV headers
include_directories("${OPENCV_NATIVE_DIR}/jni/include")

# Import OpenCV shared lib
add_library(opencv_java4 SHARED IMPORTED)
set_target_properties(
        opencv_java4
        PROPERTIES IMPORTED_LOCATION
        "${OPENCV_NATIVE_DIR}/staticlibs/${ANDROID_ABI}/libopencv_java4.so"
)

# ===== Our native library =====
# Change the source list below to match where your .cpp files actually are.
# If your files are in app/src/main/cpp:
#   native-lib.cpp
#
# If you created a separate jni folder (sibling of cpp) like in the earlier plan:
#   ../jni/native_processor.cpp
#   ../jni/opencv_processor.cpp
add_library(
        flam_native
        SHARED
        native-lib.cpp
        # ../jni/native_processor.cpp
        # ../jni/opencv_processor.cpp
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
