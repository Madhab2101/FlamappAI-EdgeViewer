# FlamappAI EdgeViewer

A real-time edge-processing pipeline built using:

* **Android (Camera2 + JNI + C++ + OpenCV + OpenGL ES 2.0)**
* **TypeScript Web Viewer**

This project demonstrates real-time camera frame acquisition, native processing using OpenCV, GPU rendering, and a simple web-based validation tool.

---

## 📱 Android App – Features

* Real-time camera stream using **Camera2**
* YUV → RGBA conversion in Kotlin
* Native processing via **JNI + C++**
* OpenCV filters:
    * Raw (no processing)
    * Grayscale
    * Canny Edge Detection
* Rendered using **OpenGL ES 2.0**
* FPS + processing time displayed live
* Clean Kotlin structure (CameraController, NativeBridge, GLTextureView)

---

## 🌐 Web Viewer – Features

Located under `/web/`:

* Displays a processed sample frame (PNG or Base64)
* Shows resolution + FPS
* TypeScript compiled to `/public/dist/index.js`
* Very small, lightweight demo viewer

Run with:

```bash
cd web
npm install
npm run build
npm start
```

Then open:

```
http://localhost:3000/
```

---

## 📂 Project Structure

```
FlamappAI-EdgeViewer/
│
├─ Pictures/                          # Screenshots and demos
│  ├─ App Demo.png
│  ├─ App Permission.png
│  └─ Web Demo.png
│
├─ app/
│  ├─ src/main/java/com/flamappai/
│  │  ├─ MainActivity.kt
│  │  ├─ camera/CameraController.kt
│  │  ├─ gl/GLTextureView.kt
│  │  └─ nativebridge/NativeProcessor.kt
│  ├─ src/main/res/layout/activity_main.xml
│  ├─ src/main/AndroidManifest.xml
│  └─ build.gradle.kts
│
├─ jni/
│  ├─ native_processor.cpp
│  ├─ opencv_processor.cpp
│  ├─ opencv_processor.hpp
│  └─ jni_utils.hpp
│
├─ gl/
│  └─ shaders/
│     ├─ textured_quad.vert
│     └─ textured_quad.frag
│
├─ web/
│  ├─ package.json
│  ├─ tsconfig.json
│  ├─ src/index.ts
│  ├─ public/
│  │  ├─ index.html
│  │  ├─ style.css
│  │  └─ dist/index.js
│  └─ README.md
│
└─ README.md
```

---

## 🛠 Setup Instructions

### **Android Setup**

#### 1. Install NDK + CMake

Android Studio → **SDK Manager → SDK Tools**:

* NDK (25.2+ recommended)
* CMake (3.22+)
* Android SDK Build-Tools

#### 2. Install OpenCV for Android

Download from:
[https://opencv.org/releases/](https://opencv.org/releases/)

Unzip to a path like:

```
D:/Android/OpenCV/OpenCV-android-sdk/
```

#### 3. Configure CMakeLists

Ensure `app/src/main/cpp/CMakeLists.txt` has the correct OpenCV path:

```cmake
set(OPENCV_ANDROID_SDK_ROOT "D:/Android/OpenCV/OpenCV-android-sdk")
```

Match this to your actual installation path.

#### 4. Build

Android Studio → **Build → Make Project**

---

### **Running the Android App**

Steps:

1. Connect a device (USB Debugging ON)
2. Open project in Android Studio
3. Click **Run ▶**
4. Grant camera permission when prompted
5. Processed camera view appears with three modes:
    - **Raw**: Original camera feed
    - **Gray**: Grayscale conversion
    - **Edges**: Canny edge detection

#### Troubleshooting

**OpenCV .so not found:**

* Verify CMake path in CMakeLists.txt
* Ensure ABI filters include `arm64-v8a` and `armeabi-v7a`
* Check that OpenCV native libraries are in correct location

**Black screen:**

* GLTextureView not receiving frames
* Check CameraController callbacks
* Verify camera permissions granted

**JNI UnsatisfiedLinkError:**

* Ensure both libraries load in correct order:
  ```kotlin
  System.loadLibrary("opencv_java4")
  System.loadLibrary("flam_native")
  ```

**Build errors:**

* Clean and rebuild: **Build → Clean Project → Rebuild Project**
* Invalidate caches: **File → Invalidate Caches / Restart**

---

## 🌐 Running the Web Viewer

### Prerequisites

* Node.js (v14+)
* npm or yarn

### Setup

```bash
cd web
npm install
```

### Build TypeScript

```bash
npm run build
```

### Start Server

```bash
npm start
```

Then open:
[http://localhost:3000](http://localhost:3000)

The viewer displays:

* Sample processed frame (add `sample_frame.png` to `web/public/`)
* Resolution + FPS information
* Placeholder if no sample frame is available

### Adding Your Own Frame

1. Take a screenshot from the Android app
2. Save it as `sample_frame.png`
3. Place it in `web/public/`
4. Refresh the browser

---

## 🧱 Architecture Overview

### **1. Camera Layer (Kotlin / Camera2)**

```
Camera2 API (YUV_420_888)
    ↓
YUV → RGBA conversion (Kotlin)
    ↓
NativeProcessor.nativeProcessFrameRgba()
```

### **2. JNI Bridge (Kotlin ↔ C++)**

* Passes RGBA byte array + dimensions
* Calls native OpenCV pipeline
* Receives processed buffer in-place
* No extra memory copies

### **3. C++ Processing (OpenCV)**

```
RGBA Mat (input)
    ↓
Grayscale conversion
    ↓
Canny Edge Detection / Raw / Gray
    ↓
RGBA Mat (output)
```

**Processing Modes:**
- `mode = 0`: Raw (no processing)
- `mode = 1`: Grayscale
- `mode = 2`: Canny edges (100, 200 thresholds)

### **4. OpenGL Rendering (GLSurfaceView)**

```
Processed RGBA buffer
    ↓
GPU Texture upload (glTexImage2D)
    ↓
Fullscreen textured quad
    ↓
GLSL vertex + fragment shaders
```

### **5. Web Viewer (TypeScript)**

```
PNG or Base64 image
    ↓
index.ts → DOM manipulation
    ↓
Display frame + stats overlay
```

---

## 📸 Screenshots

### Android App

#### Permission Request
<img src="Pictures/App-Permission.png" alt="App Permission" width="300"/>
*Camera permission dialog on first launch*

#### Real-time Processing
<img src="Pictures/App-Demo.png" alt="App Demo" width="300"/>
*Live edge detection with FPS counter*

### Web Viewer

<img src="Pictures/Web-Demo.png" alt="Web Demo" width="600"/>
*Web-based frame viewer with resolution and FPS stats*

---

## 🎯 Key Implementation Details

### Camera Controller
- Uses Camera2 API for low-level camera access
- Configures YUV_420_888 format for better performance
- Background thread handling with HandlerThread
- Non-deprecated SessionConfiguration API

### Native Processing
- Zero-copy in-place processing
- Modular C++ architecture with clean separation
- Header-only utilities (jni_utils.hpp)
- Exception-safe resource management

### OpenGL Rendering
- Double-buffered rendering with atomic reference
- Linear texture filtering for smooth display
- RENDERMODE_WHEN_DIRTY for power efficiency
- Custom GLSL shaders for texture mapping

### Web Viewer
- Strict TypeScript configuration
- Modern ES6 modules
- Graceful fallback with canvas placeholder
- Responsive design

---

## 📊 Performance Metrics

Typical performance on mid-range devices:

- **Resolution**: 640×480
- **Frame Rate**: 15-30 FPS
- **Processing Time**: 10-30ms per frame
- **Latency**: <100ms end-to-end

---


## 🚀 Future Improvements

### Performance
- [ ] GPU shader-based edge detection (compute shaders)
- [ ] Multi-threaded frame buffering
- [ ] Adaptive resolution based on device capabilities
- [ ] Hardware accelerator integration (DSP/NPU)

### Features
- [ ] Additional filters (Sobel, Gaussian blur, morphology)
- [ ] Real-time parameter adjustment UI
- [ ] Frame recording and playback
- [ ] Cloud sync for processed frames
- [ ] Real-time WebSocket streaming to web viewer

### Architecture
- [ ] Dependency injection framework
- [ ] Unit tests for native code
- [ ] Integration tests with mock camera
- [ ] CI/CD pipeline with automated builds

---

## 🔧 Dependencies

### Android
- **Kotlin**: 2.0.21
- **Gradle**: 8.13
- **NDK**: r25+
- **CMake**: 3.22.1
- **OpenCV Android SDK**: 4.12.0
- **CameraX**: 1.3.4
- **Material Components**: 1.13.0

### Web
- **TypeScript**: 5.6.0
- **http-server**: 14.1.1
- **Target**: ES6 / DOM

---

## 📄 License

This project is for educational and demonstration purposes.

---

## 👨‍💻 Author

FlamappAI EdgeViewer demonstrates practical real-time computer vision on Android with native code integration and web-based validation.

---

## 🤝 Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Make incremental commits with clear messages
4. Test thoroughly on physical devices
5. Submit a pull request

---

## 📞 Support

For issues or questions:
- Open an issue on GitHub
- Check existing documentation
- Review troubleshooting section

---

## ✅ Checklist

- [x] Android app with Camera2 integration
- [x] Native C++ OpenCV processing
- [x] OpenGL ES rendering
- [x] Mode switching (Raw/Gray/Edges)
- [x] FPS counter and performance metrics
- [x] TypeScript web viewer
- [x] Complete documentation
- [x] Screenshots and demos
- [x] Clean architecture with separation of concerns
- [x] Error handling and graceful degradation

---

**Built with ❤️ using Android NDK, OpenCV, OpenGL ES, and TypeScript**