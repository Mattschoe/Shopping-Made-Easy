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
 * Reads and saves a image of a receipt.
 * @param uiContent the onClick UI that activates the SaveImage function
 * @param onImageProcessed determines what to do after the image has been taken and processed. Often used to navigate to a different page
 */
@Composable
fun SaveImage(modifier: Modifier = Modifier,
              onImageCaptured: (Uri, Context) -> Unit,
              uiContent: @Composable (modifier: Modifier, onClick: () -> Unit) -> Unit = { _, _ -> }
    ) {
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

    //Shows image
    uiContent(modifier) {
        imageLauncher.launch(imageURI)
    }
}


/**
 * UI for saving the receipt to the program
 */
@Composable
fun SaveImageButton(modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Button(
            modifier = Modifier
                .fillMaxSize(),
            onClick = onClick,
            shape = RoundedCornerShape(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text(
                text = "Take a picture!"
            )
        }
    }
}

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

