package weberstudio.app.billigsteprodukter.MVVM

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import weberstudio.app.billigsteprodukter.ui.theme.BilligsteProdukterTheme
import weberstudio.app.billigsteprodukter.R
import java.io.File


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
                MainScreen()
            }
        }
    }

    /**
     * The main page of the UI
     */
    @Composable
    fun MainScreen() {
        Scaffold(
            topBar = { NavigationUI() }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //Save receipt
                SaveImageButtonUI(
                    modifier = Modifier
                        .weight(3f)
                        .fillMaxWidth()
                )

                //Quick actions row
                QuickActionsUI(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                //Map UI
                MapUI(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                )
            }
        }
    }


    /**
     * UI for saving the receipt to the program
     */
    @Composable
    fun SaveImageButtonUI(modifier: Modifier) {
        var previewImage by remember { mutableStateOf<Bitmap?>(null) } //For debugging

        val context = LocalContext.current
        val imageFile = File(baseContext.cacheDir, "tempImage.jpg")
        val imageURI = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", imageFile)

        //Takes image and processes it
        val imageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.TakePicture()
        ) { success ->
            if (success) {
                val bitmap = BitmapFactory.decodeFile(imageFile.absolutePath)
                previewImage = bitmap //DEBUG
                viewModel.processImage(bitmap)
            } else {
                Toast.makeText(context, "Image capture failed!", Toast.LENGTH_SHORT).show()
            }
        }

        //UI
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(onClick = { imageLauncher.launch(imageURI) }) {
                Text(text = "Take a picture!")
            }
            Spacer(Modifier.height(12.dp))
            previewImage?.let { bmp ->
                //DEBUG
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

    @Composable
    fun QuickActionsUI(modifier: Modifier) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CreateShoppingListUI(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            TempUI(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )
        }
    }

    @Composable
    fun CreateShoppingListUI(modifier: Modifier) {
        QuickActionsButton("Opret indkøbsliste", R.drawable.list, { println("Jeg vil gerne oprette min indkøbsliste!") }, modifier)
    }

    @Composable
    fun MapUI(modifier: Modifier) {
        Text(text = "mapUI")
    }

    //Temporary name until i find a use for this
    @Composable
    fun TempUI(modifier: Modifier) {
        QuickActionsButton("Temp UI", R.drawable.list, { println("Jeg vil gerne lave noget temp her!") }, modifier)

    }

    @Composable
    fun NavigationUI() {
        Text(text = "menuUI")
    }


    /**
     * Buttons for quick actions. Max 2 per row
     */
    @Composable
    fun QuickActionsButton(text: String, @DrawableRes iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(72.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = Color.
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = "Ikon af kvittering",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(16.dp)) //Padding between icon and text

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }

}