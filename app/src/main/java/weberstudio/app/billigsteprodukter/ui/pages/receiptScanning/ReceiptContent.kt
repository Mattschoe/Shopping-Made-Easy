package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import android.icu.text.DecimalFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.components.SaveImage
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.ui.ParsingState
import weberstudio.app.billigsteprodukter.ui.components.AddProductDialog
import weberstudio.app.billigsteprodukter.ui.components.AddProductToReceipt
import weberstudio.app.billigsteprodukter.ui.components.ErrorMessageLarge
import weberstudio.app.billigsteprodukter.ui.components.LogoBarHandler
import weberstudio.app.billigsteprodukter.ui.components.ProductRow
import weberstudio.app.billigsteprodukter.ui.components.TotalBar
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

    //What to show depending on the state of parsing
    if (parsingState is ParsingState.InProgress) CircularProgressIndicator()
    else if (parsingState is ParsingState.Error) {
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
    //endregion


    val products by receiptViewModel.lastReceipt.collectAsState()
    var showAddProductDialog by rememberSaveable { mutableStateOf(false) }

    //UI
    LazyColumn {
        stickyHeader{
            Column {
                LogoBarHandler(storeName = "Netto")
                TotalBar()
            }
        }
        item {
            AddProductToReceipt(addProductToReceipt = { showAddProductDialog = true } )
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
        standardStore =
    )
}