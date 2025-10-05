package weberstudio.app.billigsteprodukter.ui.components

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

/**
 * Returns *imageLauncher.launch(imageURI)* which launches the camera. Useful for launching the camera on error handling
 */
@Composable
fun launchCamera(onImageCaptured: (Uri, Context) -> Unit): () -> Unit {
    val context = LocalContext.current
    val imageURI = remember { //Uses "remember" so we dont calculate this every recomposition
        File(context.cacheDir, "tempImage.jpg")
            .let { imageFile ->
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)
            }
    }

    //Takes image and processes it
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            onImageCaptured(imageURI, context)
        } else {
            Toast.makeText(context, "Image capture failed!", Toast.LENGTH_SHORT).show()
        }
    }

    return {
        imageLauncher.launch(imageURI)
    }
}

