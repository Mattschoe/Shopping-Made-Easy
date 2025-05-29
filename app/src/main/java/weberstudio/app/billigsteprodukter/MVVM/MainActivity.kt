package weberstudio.app.billigsteprodukter.MVVM

import android.Manifest
import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import weberstudio.app.billigsteprodukter.ui.theme.BilligsteProdukterTheme


class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ViewModel>()
    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Asks for permissions
        val activityResultLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                // Handle Permission granted/rejected
                var permissionGranted = true
                permissions.entries.forEach {
                    if (it.key in REQUIRED_PERMISSIONS && it.value == false) permissionGranted = false //If permissions aren't granted
                }
                if (!permissionGranted) {
                    Toast.makeText(baseContext,
                        "Permission request denied",
                        Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(baseContext,
                        "Permissions accepted!",
                        Toast.LENGTH_SHORT).show()
                }
        }
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)

        setContent {
            BilligsteProdukterTheme {
                SaveImageButton()
            }
        }
    }

    @Composable
    fun SaveImageButton() {
        var previewImage by remember { mutableStateOf<Bitmap?>(null) }
        //Takes image and processes it
        val previewLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicturePreview()
        ) { bitmap: Bitmap? -> bitmap?.let {
            previewImage = it
            viewModel.processImage(it)
        }}

        //UI
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { previewLauncher.launch() }) {
                Text(text = "Take a picture!")
            }
            Spacer(Modifier.height(12.dp))
            previewImage?.let { bmp ->
                Image(
                    bitmap = bmp.asImageBitmap(),
                    contentDescription = "Debug preview",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(800.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                )
            }
        }
    }


}