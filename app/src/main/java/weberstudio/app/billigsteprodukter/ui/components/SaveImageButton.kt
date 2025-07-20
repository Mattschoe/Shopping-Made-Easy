package weberstudio.app.billigsteprodukter.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import java.io.File

/**
 * UI for saving the receipt to the program
 */
@Composable
fun SaveImageButton(modifier: Modifier = Modifier, onImageCaptured: (Bitmap) -> Unit) {
    var previewImage by remember { mutableStateOf<Bitmap?>(null) } //For debugging

    val context = LocalContext.current
    val imageFile = File(context.cacheDir, "tempImage.jpg") //Temp file in image directory
    val imageURI = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)

    //Takes image and processes it
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
            previewImage = bitmap //DEBUG
            onImageCaptured(bitmap)
        } else {
            Toast.makeText(context, "Image capture failed!", Toast.LENGTH_SHORT).show()
        }
    }

    //UI
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Button(
            modifier = Modifier
                .size(300.dp),
            onClick = { imageLauncher.launch(imageURI) },
            shape = CircleShape,
        ) {
            Text(
                text = "Take a picture!"
            )
        }
    }
}