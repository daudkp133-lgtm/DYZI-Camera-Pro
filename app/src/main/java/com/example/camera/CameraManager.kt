package com.example.camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceOrientedMeteringPointFactory
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.model.CameraFacing
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CameraManager(private val context: Context) {

    private var cameraProvider: ProcessCameraProvider? = null
    private var camera: Camera? = null
    private var imageCapture: ImageCapture? = null
    private var preview: Preview? = null

    private val _facing = MutableStateFlow(CameraFacing.BACK)
    val facing: StateFlow<CameraFacing> = _facing.asStateFlow()

    private val _flashMode = MutableStateFlow(ImageCapture.FLASH_MODE_OFF)
    val flashMode: StateFlow<Int> = _flashMode.asStateFlow()

    private val _zoomRatio = MutableStateFlow(1f)
    val zoomRatio: StateFlow<Float> = _zoomRatio.asStateFlow()

    private val _isCameraReady = MutableStateFlow(false)
    val isCameraReady: StateFlow<Boolean> = _isCameraReady.asStateFlow()

    suspend fun initializeCamera(
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        val provider = getCameraProvider()
        cameraProvider = provider
        bindCameraUseCases(lifecycleOwner, previewView)
    }

    private suspend fun getCameraProvider(): ProcessCameraProvider =
        suspendCancellableCoroutine { cont ->
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener({
                try {
                    cont.resume(future.get())
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }, ContextCompat.getMainExecutor(context))
        }

    fun bindCameraUseCases(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        val provider = cameraProvider ?: return

        provider.unbindAll()

        val cameraSelector = if (_facing.value == CameraFacing.BACK) {
            CameraSelector.DEFAULT_BACK_CAMERA
        } else {
            CameraSelector.DEFAULT_FRONT_CAMERA
        }

        preview = Preview.Builder()
            .build()
            .also {
                it.surfaceProvider = previewView.surfaceProvider
            }

        imageCapture = ImageCapture.Builder()
            .setFlashMode(_flashMode.value)
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
            .build()

        try {
            camera = provider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )
            _isCameraReady.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isCameraReady.value = false
        }
    }

    fun toggleCamera(lifecycleOwner: LifecycleOwner, previewView: PreviewView) {
        _facing.value = if (_facing.value == CameraFacing.BACK) {
            CameraFacing.FRONT
        } else {
            CameraFacing.BACK
        }
        bindCameraUseCases(lifecycleOwner, previewView)
    }

    fun cycleFlashMode() {
        val nextMode = when (_flashMode.value) {
            ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
            else -> ImageCapture.FLASH_MODE_OFF
        }
        _flashMode.value = nextMode
        imageCapture?.flashMode = nextMode
    }

    fun setZoom(linearZoom: Float) {
        camera?.cameraControl?.setLinearZoom(linearZoom.coerceIn(0f, 1f))
        _zoomRatio.value = linearZoom
    }

    fun tapToFocus(previewView: PreviewView, x: Float, y: Float) {
        val factory = SurfaceOrientedMeteringPointFactory(
            previewView.width.toFloat(),
            previewView.height.toFloat()
        )
        val point = factory.createPoint(x, y)
        val action = FocusMeteringAction.Builder(point).build()
        camera?.cameraControl?.startFocusAndMetering(action)
    }

    suspend fun capturePhoto(): File = suspendCancellableCoroutine { cont ->
        val capture = imageCapture
        if (capture == null) {
            cont.resumeWithException(IllegalStateException("ImageCapture not ready"))
            return@suspendCancellableCoroutine
        }

        val rawFile = File(context.cacheDir, "raw_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(rawFile).build()

        capture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    cont.resume(rawFile)
                }

                override fun onError(exception: ImageCaptureException) {
                    cont.resumeWithException(exception)
                }
            }
        )
    }
}
