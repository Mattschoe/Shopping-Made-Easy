package weberstudio.app.billigsteprodukter.ui.components

import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.logic.CameraCoordinator
import weberstudio.app.billigsteprodukter.logic.Logger
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation
import weberstudio.app.billigsteprodukter.ui.pages.home.MainPageContent
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import kotlinx.coroutines.CoroutineScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/**
 * The shell of each page in the app. Has to be applied to every page in the app.
 * The page itself owns its top bar via the [topBar] slot — build it from [PageTopBar] +
 * [PageTitle]/[EditableTitle]. Leave [topBar] empty for a page without a title (e.g. scanning).
 * @param navController the page controller
 * @param modifier the modifier that's going to be propagated to page
 * @param topBar the top bar content of the page. Defaults to nothing (no title)
 * @param floatingActionButton an **optional** action button layered on top of the UI.
 * @param pageContent the content that has to be displayed on the page. F.ex. [MainPageContent] for the "Home" page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageShell(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    floatingActionButton: (@Composable () -> Unit)? = null,
    pageContent: @Composable (PaddingValues) -> Unit,
) {
    val context = LocalContext.current
    val cameraCoordinator: CameraCoordinator = viewModel(
        viewModelStoreOwner = context as ComponentActivity
    )

    val settingsRepo = (LocalContext.current.applicationContext as ReceiptApp).settingsRepository
    val cameraLaunchRequest by settingsRepo.cameraLaunchRequest.collectAsStateWithLifecycle(initialValue = false)

    var showCamera by remember { mutableStateOf(false) }

    if (cameraLaunchRequest) {
        showCamera = true
        LaunchedEffect(cameraLaunchRequest) {
            settingsRepo.setCameraLaunchRequest(false)
        }
    }

    Box(modifier = modifier) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = topBar,
            bottomBar = {
                NavigationBar(
                    navController = navController,
                    onLaunchCamera = { showCamera = true }
                )
            },
            floatingActionButton = {
                if (floatingActionButton != null) floatingActionButton()
            },
        ) { innerPadding ->
            pageContent(innerPadding)
        }

        // Camera overlay - renders on top of everything
        if (showCamera) {
            CameraWithFlashlight(
                onImageCaptured = { uri, ctx ->
                    cameraCoordinator.onImageCaptured(uri, ctx)
                    showCamera = false
                    navController.navigate(PageNavigation.ReceiptScanning(0)) { launchSingleTop = true }
                },
                onError = { exception ->
                    Logger.log("Camera", "TakePicture Failure! ${exception.message}")
                    Toast.makeText(context, "Image capture failed!", Toast.LENGTH_SHORT).show()
                    showCamera = false
                },
                onDismiss = {
                    showCamera = false
                }
            )
        }
    }
}

/**
 * A single-line page title that auto-shrinks to fit the available width.
 * @param maxFontSize the largest size the title is allowed to render at before shrinking.
 */
@Composable
fun PageTitle(text: String, modifier: Modifier = Modifier, maxFontSize: TextUnit = 57.sp) {
    BasicText(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.displayLarge.copy(
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        ),
        maxLines = 1,
        autoSize = TextAutoSize.StepBased(
            minFontSize = 18.sp,
            maxFontSize = maxFontSize,
            stepSize = 2.sp
        )
    )
}

/**
 * The standard top bar wrapper. Pass a [title] slot ([PageTitle] or [EditableTitle]) and
 * optional [actions] (e.g. [SettingsAction]).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageTopBar(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    title: @Composable () -> Unit,
) {
    TopAppBar(
        modifier = modifier,
        title = title,
        actions = actions
    )
}

/**
 * The settings icon button. Used as a top bar action on the home page.
 */
@Composable
fun SettingsAction(navController: NavController) {
    IconButton(onClick = {
        navController.navigate(PageNavigation.Settings) { launchSingleTop = true }
    }) {
        Icon(
            ImageVector.vectorResource(R.drawable.settings_icon),
            contentDescription = "Indstillinger"
        )
    }
}

/**
 * A page title that doubles as an inline editor. Tapping the title opens the keyboard with no
 * border/decoration; the change is committed via [onCommit] on the "Done" key or when focus leaves.
 */
@Composable
fun EditableTitle(name: String, onCommit: (String) -> Unit, modifier: Modifier = Modifier) {
    // Mindre end de statiske titler, så et redigeringsfelt kan rummes i top baren uden at flyde over.
    val titleFontSize = 34.sp
    var isEditing by remember { mutableStateOf(false) }
    var temp by remember(name) { mutableStateOf(TextFieldValue(name)) }
    //Optimistisk visningsnavn: viser straks det gemte navn, så vi undgår et frame med det gamle navn
    //mens onCommit runder turen gennem ViewModel → DB → Flow. Re-seedes når det rigtige navn kommer ind.
    var displayName by remember(name) { mutableStateOf(name) }
    var hasInitialFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun commit() {
        //Undgå dobbelt-commit + commit mens feltet allerede er på vej ud af komposition
        if (!isEditing) return
        isEditing = false
        hasInitialFocus = false
        val newName = temp.text.trim()
        //Tomt navn eller uændret navn skal ikke gemmes
        if (newName.isNotEmpty() && newName != name) {
            displayName = newName
            onCommit(newName)
        }
        //Skjul tastaturet — undgå focusManager.clearFocus() da feltet fjernes samtidig (crasher)
        keyboardController?.hide()
    }

    if (isEditing) {
        BasicTextField(
            value = temp,
            onValueChange = { temp = it },
            textStyle = MaterialTheme.typography.displayLarge.copy(
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            ),
            singleLine = true,
            cursorBrush = SolidColor(MaterialTheme.colorScheme.onBackground),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { commit() }),
            modifier = modifier
                .focusRequester(focusRequester)
                .onFocusChanged { focusState ->
                    if (!isEditing) return@onFocusChanged
                    if (focusState.isFocused) hasInitialFocus = true
                    else if (hasInitialFocus) commit()
                }
        )
        LaunchedEffect(Unit) {
            delay(50)
            focusRequester.requestFocus()
        }
    } else {
        PageTitle(
            text = displayName,
            maxFontSize = titleFontSize,
            modifier = modifier.clickable {
                //Markøren placeres i slutningen af teksten ved redigeringsstart
                temp = TextFieldValue(name, selection = TextRange(name.length))
                isEditing = true
                hasInitialFocus = false
            }
        )
    }
}

@Composable
fun NavigationBar(
    navController: NavController,
    onLaunchCamera: () -> Unit
) {
    //region SCANNING VALIDATION
    val settingsRepo = (LocalContext.current.applicationContext as ReceiptApp).settingsRepository
    val scope = rememberCoroutineScope()
    var showCoop365OptionsDialog by remember { mutableStateOf(false) }
    //endregion

    NavigationBar(
        windowInsets = NavigationBarDefaults.windowInsets,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        //Home
        NavigationBarItem(
            icon = {
                Icon(
                    ImageVector.vectorResource(R.drawable.home_icon),
                    contentDescription = "Hjem",
                    modifier = Modifier.size(36.dp),
                )
            },
            selected = currentDestination?.hasRoute<PageNavigation.Home>() == true,
            enabled = currentDestination?.hasRoute<PageNavigation.Home>() != true,
            onClick = {
                navController.navigate(PageNavigation.Home) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            },
        )

        //Shopping list
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.list_icon),
                    contentDescription = "Shopping lister",
                    modifier = Modifier.size(36.dp),
                )
            },
            selected = currentDestination?.hasRoute<PageNavigation.ShoppingList>() == true || currentDestination?.hasRoute<PageNavigation.ShoppingListUndermenu>() == true,
            enabled = !(currentDestination?.hasRoute<PageNavigation.ShoppingList>() == true || currentDestination?.hasRoute<PageNavigation.ShoppingListUndermenu>() == true),
            onClick = {
                navController.navigate(PageNavigation.ShoppingList) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )

        //Historik
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(id = R.drawable.pricetag_icom),
                    contentDescription = "Database",
                    modifier = Modifier.size(36.dp),
                )
            },
            selected = currentDestination?.hasRoute<PageNavigation.Database>() == true,
            enabled = currentDestination?.hasRoute<PageNavigation.Database>() != true,
            onClick = {
                navController.navigate(PageNavigation.Database) {
                    popUpTo(navController.graph.startDestinationId) { inclusive = false }
                    launchSingleTop = true
                }
            }
        )

        //Receipt scanning
        NavigationBarItem(
            icon = {
                Icon(
                    painterResource(R.drawable.camera_icon),
                    contentDescription = "Scan kvittering",
                    modifier = Modifier.size(36.dp),
                )
            },
            selected = currentDestination?.hasRoute<PageNavigation.ReceiptScanning>() == true,
            onClick = {
                scope.launch {
                    val coopOption = settingsRepo.coop365Option.firstOrNull()
                    if (coopOption == null) showCoop365OptionsDialog = true
                    else onLaunchCamera()
                }
            }
        )
    }

    //region DIALOGS
    if (showCoop365OptionsDialog) {
        Coop365OptionDialog(
            onDismiss = { showCoop365OptionsDialog = false },
            onConfirm = { option ->
                scope.launch {
                    settingsRepo.setCoop365Option(option)
                    showCoop365OptionsDialog = false
                    onLaunchCamera()
                }
            }
        )
    }
    //endregion
}

