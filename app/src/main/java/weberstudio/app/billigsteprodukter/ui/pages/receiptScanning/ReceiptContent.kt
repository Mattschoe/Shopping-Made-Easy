package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.logic.Formatter.formatFloatToDanishCurrency
import weberstudio.app.billigsteprodukter.ui.ParsingState
import weberstudio.app.billigsteprodukter.ui.ReceiptUIState
import weberstudio.app.billigsteprodukter.ui.components.AddProductDialog
import weberstudio.app.billigsteprodukter.ui.components.AddProductToReceiptButton
import weberstudio.app.billigsteprodukter.ui.components.ErrorMessageLarge
import weberstudio.app.billigsteprodukter.ui.components.LogoBarHandler
import weberstudio.app.billigsteprodukter.ui.components.LogoBarSkeleton
import weberstudio.app.billigsteprodukter.ui.components.ModifyProductDialog
import weberstudio.app.billigsteprodukter.ui.components.ProductRow
import weberstudio.app.billigsteprodukter.ui.components.ProductRowSkeleton
import weberstudio.app.billigsteprodukter.ui.components.TotalAndFilterRow
import weberstudio.app.billigsteprodukter.ui.components.TotalAndFilterRowSkeleton
import weberstudio.app.billigsteprodukter.ui.components.launchCamera
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReceiptScanningContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    cameraViewModel: CameraViewModel,
    receiptViewModel: ReceiptScanningViewModel
) {
    //region Checks for parsing errors first:
    val parsingState by cameraViewModel.getParserState()
    val uiState by receiptViewModel.uiState.collectAsState()
    var showAddProductDialog by rememberSaveable { mutableStateOf(false) }
    val launchCamera = launchCamera(
        onImageCaptured = { uri, context ->
            cameraViewModel.processImage(uri, context)
        }
    )

    //Handles state changes based on parsing state
    LaunchedEffect(parsingState) {
        when (parsingState) {
            ParsingState.InProgress -> { receiptViewModel.showLoadingState() }
            is ParsingState.Success -> { cameraViewModel.clearParserState() }
            else -> {  }
        }
    }

    if (parsingState is ParsingState.Error) {
        val errorMessage = (parsingState as ParsingState.Error).message
        ErrorMessageLarge(
            errorTitle = "Fejl i scanning!",
            errorMessage = errorMessage,
            onDismissRequest = {
                cameraViewModel.clearParserState()
                navController.navigate(PageNavigation.Home.route)
            },
            onConfirmError = {
                cameraViewModel.clearParserState()
                launchCamera()
            },
            onDismissError = { cameraViewModel.clearParserState() }
        )
    }
    //endregion

    when (val currentState = uiState) {
        is ReceiptUIState.Loading -> { LoadingSkeleton(modifier) }
        is ReceiptUIState.Success -> {
            ReceiptContent(
                modifier = modifier,
                products = currentState.products,
                store = currentState.store,
                onAddProductClick = { showAddProductDialog = true },
                modifyProduct = { newProduct -> receiptViewModel.updateProduct(newProduct) }
            )
        }
        is ReceiptUIState.Empty -> {
            LazyColumn(modifier = modifier) {
                //Nothing bruh
            }
        }
    }

    //region DIALOGS
    //Add Product Dialog
    if (uiState is ReceiptUIState.Success) {
        val store = (uiState as ReceiptUIState.Success).store
        AddProductDialog(
            showDialog = showAddProductDialog,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { name, price, productStore ->
                store.let {
                    cameraViewModel.addProductToCurrentReceipt(name, price, it)
                }
                showAddProductDialog = false
            },
            standardStore = store
        )
    }
    //endregion
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoadingSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                //Logo skeleton
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LogoBarSkeleton(modifier = Modifier.fillMaxWidth())
                }

                TotalAndFilterRowSkeleton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp)
                )
            }
        }

        items(8) {
            ProductRowSkeleton()
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ReceiptContent(
    modifier: Modifier = Modifier,
    products: List<Product>,
    store: weberstudio.app.billigsteprodukter.logic.Store?,
    onAddProductClick: () -> Unit,
    modifyProduct: (Product) -> Unit
) {
    var product2Modify by remember { mutableStateOf<Product?>(null) }

    LazyColumn(modifier = modifier) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                //Store logo
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (store != null) {
                        LogoBarHandler(modifier = Modifier.fillMaxSize(), storeName = store.name)
                    }
                }

                //Total and filter
                TotalAndFilterRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    totalPrice = formatFloatToDanishCurrency(products.sumOf { it.price.toDouble() }.toFloat()),
                    filterMenuOnClick = { /* TODO: Implement filter */ }
                )
            }
        }

        //Add product button
        item {
            AddProductToReceiptButton(addProductToReceipt = onAddProductClick)
        }

        //Product list
        items(products) { product ->
            ProductRow(
                productName = product.name,
                productPrice = formatFloatToDanishCurrency(product.price) + "kr",
                onThreeDotMenuClick = { /* TODO: Implement menu */ },
                onClick = {
                    product2Modify = product
                }
            )
        }
    }

    product2Modify?.let { product ->
        ModifyProductDialog(
            product = product,
            onDismiss = { product2Modify = null },
            onConfirm = { newProduct ->
                modifyProduct(newProduct)
                product2Modify = null
            }
        )
    }
}