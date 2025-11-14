cmake_minimum_required(VERSION 3.22.1)

project("flamappai")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(ANDROID_STL "c++_shared")

# ===== Get project root =====
get_filename_component(PROJECT_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../../../.." ABSOLUTE)
set(JNI_DIR "${PROJECT_ROOT}/jni")

message(STATUS "Project Root: ${PROJECT_ROOT}")
message(STATUS "JNI Directory: ${JNI_DIR}")

# ===== OpenCV Android SDK path =====
set(OPENCV_ANDROID_SDK_ROOT "D:/Android/OpenCV/OpenCV-android-sdk")
set(OPENCV_NATIVE_DIR "${OPENCV_ANDROID_SDK_ROOT}/sdk/native")

# Include OpenCV headers
include_directories("${OPENCV_NATIVE_DIR}/jni/include")

# Include jni directory headers
include_directories("${JNI_DIR}")

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
        ${JNI_DIR}/native_processor.cpp
        ${JNI_DIR}/opencv_processor.cpp
)

# Find Android log library
find_library(log-lib log)

# Link everything together
target_link_libraries(
        flam_native
        opencv_java4
        ${log-lib}
        android
        GLESv2
)
