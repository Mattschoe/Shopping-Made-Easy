package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import android.icu.text.DecimalFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.ui.ParsingState
import weberstudio.app.billigsteprodukter.ui.components.AddProductDialog
import weberstudio.app.billigsteprodukter.ui.components.AddProductToReceiptButton
import weberstudio.app.billigsteprodukter.ui.components.ErrorMessageLarge
import weberstudio.app.billigsteprodukter.ui.components.LogoBarHandler
import weberstudio.app.billigsteprodukter.ui.components.ProductRow
import weberstudio.app.billigsteprodukter.ui.components.SaveImage
import weberstudio.app.billigsteprodukter.ui.components.TotalAndFilterRow
import weberstudio.app.billigsteprodukter.ui.components.launchCamera
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation

/**
 * @param uiContent the UI that activates the [SaveImage] function
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReceiptScanningContent(modifier: Modifier = Modifier, navController: NavController, cameraViewModel: CameraViewModel, receiptViewModel: ReceiptScanningViewModel) {
    //region Checks for parsing errors first:
    val parsingState by cameraViewModel.getParserState()
    val cameraScope = rememberCoroutineScope()
    val launchCamera = launchCamera(
        onImageCaptured = { uri, context ->
            cameraScope.launch { cameraViewModel.processImage(uri, context) }
        }
    )

    var store: Store? = null

    //What to show depending on the state of parsing
    when (parsingState) {
        ParsingState.InProgress -> { CircularProgressIndicator() }
        is ParsingState.Error -> {
            val errorMessage = (parsingState as ParsingState.Error).message
            ErrorMessageLarge(
                "Fejl i scanning!",
                errorMessage,
                onDismissRequest = {
                    cameraViewModel.clearParserState()
                    navController.navigate(PageNavigation.Home.route)
                },
                onConfirmError = { launchCamera() }, //Launches camera again if user clicks "Prøv igen"
                onDismissError = { } //Goes back to last screen if user presses "Cancel"
            )
        }
        is ParsingState.Success -> { store = (parsingState as ParsingState.Success).parsedStore }
        else -> println(parsingState)

    }
    //endregion


    val products by receiptViewModel.lastReceipt.collectAsState()
    var showAddProductDialog by rememberSaveable { mutableStateOf(false) }

    //UI
    LazyColumn(
        modifier = modifier
    ) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background) //Før padding så det dækker helt
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (store != null) LogoBarHandler(modifier = Modifier.fillMaxSize(), storeName = store.name)
                }
                Row(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TotalAndFilterRow(modifier = Modifier.fillMaxSize(), totalPrice =  "193,5", filterMenuOnClick =  { })
                }
            }
        }
        item {
            AddProductToReceiptButton(addProductToReceipt = { showAddProductDialog = true } )
        }
        items(products) { product ->
            ProductRow(product.name, DecimalFormat("#.##").format(product.price) + "kr") {
            }
        }
    }

    //Hidden UI
    AddProductDialog(
        showDialog = showAddProductDialog,
        onDismiss = { showAddProductDialog = false },
        onConfirm = { name, price, store ->
            cameraViewModel.addProductToCurrentReceipt(name, price, store)
            showAddProductDialog = false
        },
        standardStore = store
    )
}