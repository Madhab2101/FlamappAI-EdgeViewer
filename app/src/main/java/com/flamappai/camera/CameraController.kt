package com.flamappai.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import androidx.core.content.ContextCompat

typealias FrameCallback = (rgba: ByteArray, width: Int, height: Int) -> Unit

class CameraController(
    private val context: Context,
    private val size: Size = Size(640, 480),
    private val onFrame: FrameCallback
) {

    private var cameraDevice: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null
    private var bgThread: HandlerThread? = null
    private var bgHandler: Handler? = null

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    fun start() {
        startBackground()
        openCamera()
    }

    fun stop() {
        session?.close()
        session = null
        cameraDevice?.close()
        cameraDevice = null
        imageReader?.close()
        imageReader = null
        stopBackground()
    }

    private fun openCamera() {
        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = manager.cameraIdList.firstOrNull { id ->
            val chars = manager.getCameraCharacteristics(id)
            chars.get(CameraCharacteristics.LENS_FACING) ==
                    CameraCharacteristics.LENS_FACING_BACK
        } ?: manager.cameraIdList.first()

        imageReader = ImageReader.newInstance(
            size.width,
            size.height,
            ImageFormat.YUV_420_888,
            2
        )
        imageReader?.setOnImageAvailableListener(::onImageAvailable, bgHandler)

        // Check permission before opening camera
        if (!hasPermission()) {
            return
        }

        // Suppress the security exception warning since we've checked permission
        @Suppress("MissingPermission")
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) {
                cameraDevice = device
                createSession()
            }

            override fun onDisconnected(device: CameraDevice) {
                device.close()
                cameraDevice = null
            }

            override fun onError(device: CameraDevice, error: Int) {
                device.close()
                cameraDevice = null
            }
        }, bgHandler)
    }

    private fun createSession() {
        val device = cameraDevice ?: return
        val surface = imageReader?.surface ?: return

        // Use the new createCaptureSession with OutputConfiguration (not deprecated)
        val outputConfig = android.hardware.camera2.params.OutputConfiguration(surface)
        val sessionConfig = android.hardware.camera2.params.SessionConfiguration(
            android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR,
            listOf(outputConfig),
            context.mainExecutor,
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(s: CameraCaptureSession) {
                    session = s
                    val builder = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    builder.addTarget(surface)
                    builder.set(
                        CaptureRequest.CONTROL_AF_MODE,
                        CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO
                    )
                    s.setRepeatingRequest(builder.build(), null, bgHandler)
                }

                override fun onConfigureFailed(s: CameraCaptureSession) {}
            }
        )
        device.createCaptureSession(sessionConfig)
    }

    private fun onImageAvailable(reader: ImageReader) {
        val image = reader.acquireLatestImage() ?: return
        val w = image.width
        val h = image.height
        val rgba = yuvToRgba(image)
        image.close()
        // rgba is non-null because yuvToRgba always returns a ByteArray
        onFrame(rgba, w, h)
    }

    // Simple YUV->RGBA using NV21 + JPEG (not fastest but OK for assignment)
    // Changed return type from ByteArray? to ByteArray since it never returns null
    private fun yuvToRgba(image: Image): ByteArray {
        val width = image.width
        val height = image.height

        val yPlane = image.planes[0]
        val uPlane = image.planes[1]
        val vPlane = image.planes[2]

        val ySize = yPlane.buffer.remaining()
        val uSize = uPlane.buffer.remaining()
        val vSize = vPlane.buffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yPlane.buffer.get(nv21, 0, ySize)

        val uvPixelStride = uPlane.pixelStride
        val uvRowStride = uPlane.rowStride
        val uvWidth = width / 2
        val uvHeight = height / 2

        val uBuffer = uPlane.buffer
        val vBuffer = vPlane.buffer
        var offset = ySize
        val row = ByteArray(uvWidth * 2)

        for (rowIndex in 0 until uvHeight) {
            val rowStart = rowIndex * uvRowStride
            uBuffer.position(rowStart)
            vBuffer.position(rowStart)
            var col = 0
            while (col < uvWidth) {
                val u = uBuffer.get()
                val v = vBuffer.get()
                row[col * 2] = v
                row[col * 2 + 1] = u
                col++
                if (uvPixelStride > 1 && col < uvWidth) {
                    uBuffer.position(uBuffer.position() + uvPixelStride - 1)
                    vBuffer.position(vBuffer.position() + uvPixelStride - 1)
                }
            }
            System.arraycopy(row, 0, nv21, offset, row.size)
            offset += row.size
        }

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 80, out)
        val jpegBytes = out.toByteArray()

        val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)
        val rgba = ByteArray(width * height * 4)
        val buffer = java.nio.ByteBuffer.wrap(rgba)
        bitmap.copyPixelsToBuffer(buffer)
        bitmap.recycle()
        return rgba
    }

    private fun startBackground() {
        bgThread = HandlerThread("CameraBG").also {
            it.start()
            bgHandler = Handler(it.looper)
        }
    }

    private fun stopBackground() {
        bgThread?.quitSafely()
        bgThread?.join()
        bgThread = null
        bgHandler = null
    }
}