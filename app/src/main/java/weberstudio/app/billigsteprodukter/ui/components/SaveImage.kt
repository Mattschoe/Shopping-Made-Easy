package weberstudio.app.billigsteprodukter.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import weberstudio.app.billigsteprodukter.logic.Logger
import java.io.File

/**
 * Returns *imageLauncher.launch(imageURI)* which launches the camera. Useful for launching the camera on error handling
 */
@Composable
fun launchCamera(onImageCaptured: (Uri, Context) -> Unit): () -> Unit {
    val tag = "launchCamera"
    val context = LocalContext.current
    val imageURI = remember {
        File(context.cacheDir, "tempImage.jpg").let { imageFile ->
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
        }
    }

    //Takes image and processes it
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            Logger.log(tag, "TakePicture success!")
            onImageCaptured(imageURI, context)
        } else {
            Logger.log(tag, "TakePicture Failure!")
            Toast.makeText(context, "Image capture failed!", Toast.LENGTH_SHORT).show()
        }
    }

    return {
        imageLauncher.launch(imageURI)
    }
}

