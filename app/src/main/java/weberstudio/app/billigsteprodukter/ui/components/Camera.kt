package weberstudio.app.billigsteprodukter.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.logic.Logger
import java.io.File

/**
 * Returns *imageLauncher.launch(imageURI)* which launches the camera. Useful for launching the camera on error handling
 */
@Composable
fun launchCamera(onImageCaptured: (Uri, Context) -> Unit): () -> Unit {
    val tag = "launchCamera"
    Logger.log(tag, "Starting camera launch")
    val context = LocalContext.current

    var showCamera by remember { mutableStateOf(false) }

    if (showCamera) {
        CameraWithFlashlight(
            onImageCaptured = { uri, ctx ->
                Logger.log(tag, "TakePicture success!")
                showCamera = false
                onImageCaptured(uri, ctx)
            },
            onError = { exception ->
                Logger.log(tag, "TakePicture Failure! ${exception.message}")
                Toast.makeText(context, "Image capture failed!", Toast.LENGTH_SHORT).show()
                showCamera = false
            },
            onDismiss = { showCamera = false }
        )
    }

    return {
        showCamera = true
    }
}

@Composable
fun CameraWithFlashlight(
    onImageCaptured: (Uri, Context) -> Unit,
    onError: (Exception) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var camera by remember { mutableStateOf<Camera?>(null) }

    val previewView = remember { PreviewView(context) }

    LaunchedEffect(cameraProviderFuture) {
        val cameraProvider = cameraProviderFuture.get()
        val preview = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        imageCapture = ImageCapture.Builder()
            .setFlashMode(ImageCapture.FLASH_MODE_OFF)
            .build()

        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            camera = cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageCapture
            )

            // Enable torch (flashlight) mode
            camera?.cameraControl?.enableTorch(true)

        } catch (e: Exception) {
            onError(e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            camera?.cameraControl?.enableTorch(false)
            cameraProviderFuture.get().unbindAll()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Capture button
        FloatingActionButton(
            onClick = {
                val outputFile = File(context.cacheDir, "tempImage_${System.currentTimeMillis()}.jpg")
                val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                imageCapture?.takePicture(
                    outputOptions,
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                outputFile
                            )
                            onImageCaptured(uri, context)
                        }

                        override fun onError(exception: ImageCaptureException) {
                            onError(exception)
                        }
                    }
                )
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .size(96.dp)
                .padding(bottom = 32.dp)
        ) {
            Icon(ImageVector.vectorResource(R.drawable.camera_icon), contentDescription = "Take picture")
        }

        // Cancel button
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Close, contentDescription = "Close camera")
        }
    }
}

