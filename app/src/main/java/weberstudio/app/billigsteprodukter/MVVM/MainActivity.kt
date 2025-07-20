package weberstudio.app.billigsteprodukter.MVVM

import android.Manifest
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import weberstudio.app.billigsteprodukter.ui.theme.BilligsteProdukterTheme
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.ui.theme.ThemeDarkGreen
import weberstudio.app.billigsteprodukter.ui.theme.ThemeLightGreen
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
                    .padding(innerPadding)
                    .padding(12.dp) //Standard padding from screen edge
                    .border(1.dp, Color.Red, RoundedCornerShape(8.dp)) //Debug
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                //Save receipt
                SaveImageButton(
                    modifier = Modifier
                        .weight(1.25f)
                        .fillMaxSize()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)) //Debug
                )

                //Quick actions row
                QuickActionsUI(
                    modifier = Modifier
                        .wrapContentSize(align = Alignment.BottomCenter)
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)) //Debug
                )

                //Map UI
                MapUI(
                    modifier = Modifier
                        .weight(0.75f)
                        .fillMaxWidth()
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp)) //Debug
                )
            }
        }
    }


    /**
     * UI for saving the receipt to the program
     */
    @Composable
    fun SaveImageButton(modifier: Modifier) {
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

        /*
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
            } */
    }

    /**
     * The layout UI for the quick actions buttons
     */
    @Composable
    fun QuickActionsUI(modifier: Modifier) {
        Row(
            modifier = modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CreateShoppingListUI(
                modifier = Modifier
                    .weight(1f, fill = false)
            )

            TempUI(
                modifier = modifier
                    .weight(1f,  fill = false)
            )
        }
    }

    /**
     * Buttons for quick actions. Max 2 per row
     */
    @Composable
    fun QuickActionsButton(text: String, @DrawableRes iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
        Surface(
            modifier = modifier
                .height(72.dp)
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            color = ThemeLightGreen
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

    @Composable
    fun CreateShoppingListUI(modifier: Modifier) {
        QuickActionsButton("Opret indkøbsliste", R.drawable.list, { println("Jeg vil gerne oprette min indkøbsliste!") }, modifier)
    }

    @Composable
    fun MapUI(modifier: Modifier) {
        Box(
            modifier = modifier
        ) {
            Text(
                text = "Her skal der være et kort! :)",
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }

    //Temporary name until i find a use for this
    @Composable
    fun TempUI(modifier: Modifier) {
        QuickActionsButton("Temp UI", R.drawable.list, { println("Jeg vil gerne lave noget temp her!") }, modifier)
    }

    @Composable
    fun NavigationUI(modifier: Modifier = Modifier) {
        Row(
            modifier = modifier
        ) {
            IconButton(
                onClick = { NavigationDrawerUI() }
            ) { }
        }
    }

    @Composable
    fun NavigationDrawerUI() {

    }
}