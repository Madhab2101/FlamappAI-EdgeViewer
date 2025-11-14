package com.flamappai

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Size
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.flamappai.camera.CameraController
import com.flamappai.gl.GLTextureView
import com.flamappai.nativebridge.NativeProcessor
import com.flamappai.R
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private lateinit var glView: GLTextureView
    private lateinit var btnMode: Button
    private lateinit var txtFps: TextView

    private lateinit var cameraController: CameraController
    private var mode = 2
    private var lastFrameTime = 0L

    private val permissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            if (granted) startCamera()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glView = findViewById(R.id.glView)
        btnMode = findViewById(R.id.btnMode)
        txtFps = findViewById(R.id.txtFps)

        val size = Size(640, 480)

        cameraController = CameraController(this, size) { rgba, w, h ->

            val ms = measureTimeMillis {
                NativeProcessor.nativeProcessFrameRgba(rgba, w, h, mode)
            }

            glView.updateFrame(rgba, w, h)
            updateFps(ms)
        }

        btnMode.setOnClickListener {
            mode = (mode + 1) % 3
            btnMode.text = when (mode) {
                0 -> "Mode: Raw"
                1 -> "Mode: Gray"
                else -> "Mode: Edges"
            }
        }
    }

    private fun updateFps(procMs: Long) {
        val now = System.currentTimeMillis()
        if (lastFrameTime != 0L) {
            val delta = now - lastFrameTime
            val fps = 1000f / delta.coerceAtLeast(1)
            runOnUiThread {
                txtFps.text = "FPS: %.1f (proc %d ms)".format(fps, procMs)
            }
        }
        lastFrameTime = now
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()

        if (cameraController.hasPermission()) {
            NativeProcessor.nativeInit(640, 480)
            startCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onPause() {
        cameraController.stop()
        NativeProcessor.nativeRelease()
        glView.onPause()
        super.onPause()
    }

    private fun startCamera() {

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        cameraController.start()
    }
}
