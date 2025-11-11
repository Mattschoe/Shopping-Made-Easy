package weberstudio.app.billigsteprodukter.ui.pages.receiptScanning

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.isEqualTo
import weberstudio.app.billigsteprodukter.logic.CameraCoordinator
import weberstudio.app.billigsteprodukter.logic.Formatter.formatFloatToDanishCurrency
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.logic.parsers.StoreParser.ScanValidation
import weberstudio.app.billigsteprodukter.ui.ParsingState
import weberstudio.app.billigsteprodukter.ui.ReceiptUIState
import weberstudio.app.billigsteprodukter.ui.components.AddProductDialog
import weberstudio.app.billigsteprodukter.ui.components.AddProductToReceiptButton
import weberstudio.app.billigsteprodukter.ui.components.AddProductToReceiptSkeleton
import weberstudio.app.billigsteprodukter.ui.components.ErrorMessageLarge
import weberstudio.app.billigsteprodukter.ui.components.LogoBarHandler
import weberstudio.app.billigsteprodukter.ui.components.LogoBarSkeleton
import weberstudio.app.billigsteprodukter.ui.components.ModifyProductDialog
import weberstudio.app.billigsteprodukter.ui.components.ModifyTotalDialog
import weberstudio.app.billigsteprodukter.ui.components.ProductCard
import weberstudio.app.billigsteprodukter.ui.components.ProductRow
import weberstudio.app.billigsteprodukter.ui.components.ProductRowSkeleton
import weberstudio.app.billigsteprodukter.ui.components.ReceiptTotalCard
import weberstudio.app.billigsteprodukter.ui.components.ReceiptTotalSkeleton
import weberstudio.app.billigsteprodukter.ui.components.launchCamera
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation
import kotlin.math.max
import kotlin.math.roundToInt


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ReceiptScanningContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    viewModel: ReceiptViewModel,
    cameraCoordinator: CameraCoordinator
) {
    val tag = remember { "ReceiptScanningContent" }

    val parsingState by viewModel.parsingState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var showAddProductDialog by rememberSaveable { mutableStateOf(false) }
    var showModifyTotalDialog by rememberSaveable { mutableStateOf(false) }

    // Check if there's a pending camera capture to process
    val pendingCapture by cameraCoordinator.pendingImageCapture.collectAsState()
    val pendingScanValidation by cameraCoordinator.pendingScanValidation.collectAsState()

    LaunchedEffect(pendingCapture) {
        pendingCapture?.let { capture ->
            viewModel.processImage(capture.uri, capture.context, cameraCoordinator)
            cameraCoordinator.clearPendingCapture()
        }
    }

    LaunchedEffect(uiState, pendingScanValidation) {
        //Only apply validation when we're showing a Success state
        if (uiState is ReceiptUIState.Success) {
            pendingScanValidation?.let { pending ->
                val currentReceiptID = (uiState as ReceiptUIState.Success).products.firstOrNull()?.receiptID

                if (currentReceiptID == pending.receiptID) {
                    viewModel.applyScanValidation(pending.validation)
                    cameraCoordinator.clearScanValidation()
                }
            }
        }
    }

    val launchCamera = launchCamera(
        onImageCaptured = { uri, context ->
            viewModel.processImage(uri, context, cameraCoordinator)
        }
    )



    //region PARSING STATE HANDLING
    LaunchedEffect(parsingState) {
        when (val state = parsingState) {
            ParsingState.InProgress -> {
                viewModel.showLoadingState()
            }
            is ParsingState.Success -> {
                navController.navigate(PageNavigation.createReceiptRoute(state.receiptID))
                viewModel.clearParsingState()
            }
            is ParsingState.Error -> {
                Log.e("Receipt Scanning ERROR:", state.message)
            }
            else -> { }
        }
    }

    if (parsingState is ParsingState.Error) {
        val errorMessage = (parsingState as ParsingState.Error).message
        ErrorMessageLarge(
            errorTitle = "Fejl i scanning!",
            errorMessage = errorMessage,
            onDismissRequest = {
                viewModel.clearParsingState()
                navController.navigate(PageNavigation.Home.route)
            },
            onConfirmError = {
                viewModel.clearParsingState()
                launchCamera()
            },
            onDismissError = { viewModel.clearParsingState() }
        )
    }
    //endregion

    //region UI
    when (val currentState = uiState) {
        is ReceiptUIState.Loading -> {
            LoadingSkeleton(modifier)
        }
        is ReceiptUIState.Success -> {
            ReceiptContent(
                modifier = modifier,
                products = currentState.products,
                store = currentState.store,
                receiptTotal = currentState.receiptTotal,
                errors = currentState.errors,
                onAddProductClick = { showAddProductDialog = true },
                onModifyTotal = { showModifyTotalDialog = true},
                modifyProduct = { newProduct ->
                    viewModel.updateProduct(newProduct)
                },
                onDeleteProduct = { product2Delete -> viewModel.deleteProduct(product2Delete) }
            )
        }
        is ReceiptUIState.Empty -> {
            LazyColumn(modifier = modifier) {
                // Empty state
            }
        }
    }
    //endregion

    //region DIALOGS
    if (uiState is ReceiptUIState.Success) {
        val store = (uiState as ReceiptUIState.Success).store

        AddProductDialog(
            showDialog = showAddProductDialog,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { name, price, productStore ->
                viewModel.addProductToCurrentReceipt(name, price, productStore)
                showAddProductDialog = false
            },
            standardStore = store
        )

        ModifyTotalDialog(
            showDialog = showModifyTotalDialog,
            originalTotal = (uiState as ReceiptUIState.Success).receiptTotal,
            hasTotalError = (uiState as ReceiptUIState.Success).errors?.totalError == true,
            onDismiss = {
                showModifyTotalDialog = false
            },
            onConfirm =  { newTotal ->
                viewModel.updateTotalForSelectedReceipt(newTotal)
                showModifyTotalDialog = false
            },
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
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LogoBarSkeleton(modifier = Modifier.fillMaxWidth())
                }
                Spacer(Modifier.height(4.dp))
                AddProductToReceiptSkeleton()
            }
        }
        //Total
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReceiptTotalSkeleton()
                }
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
    store: Store?,
    receiptTotal: Float,
    errors: ScanValidation?,
    onAddProductClick: () -> Unit,
    onModifyTotal: () -> Unit,
    modifyProduct: (Product) -> Unit,
    onDeleteProduct: (Product) -> Unit
) {
    //Drag functionality
    val density = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    var isDragging by remember { mutableStateOf(false) }
    var draggedProduct by remember { mutableStateOf<Product?>(null) }
    var dragTopLeftPx by remember { mutableStateOf(Offset.Zero) }
    var touchOffsetWithinCard by remember { mutableStateOf(Offset.Zero) }
    var draggedCardSize by remember { mutableStateOf(IntSize(0,0)) }
    var trashRectBoundsPx by remember { mutableStateOf<Rect?>(null) }
    var hoverOverTrash by remember { mutableStateOf(false) }
    var isPerformingDeleteAnimation by remember { mutableStateOf(false) }

    var product2Modify by remember { mutableStateOf<Product?>(null) }
    var hasFixedTotalError by remember { mutableStateOf(false) }

    LazyColumn(modifier = modifier) {
        stickyHeader {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (store != null) {
                        LogoBarHandler(modifier = Modifier.fillMaxWidth(), storeName = store.name)
                    }
                }
                Spacer(Modifier.height(4.dp))
                AddProductToReceiptButton(addProductToReceipt = onAddProductClick)
            }
        }

        //Total
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable(onClick = {
                        onModifyTotal()
                        hasFixedTotalError = true
                    })
            ) {
                Row (
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ReceiptTotalCard(
                        totalPrice = formatFloatToDanishCurrency(receiptTotal),
                        totalError = errors?.totalError == true && !hasFixedTotalError
                    )
                }
            }
        }

        items(
            items = products,
            key = { product -> product.databaseID }
        ) { product ->
            var cardTopLeftPx by remember { mutableStateOf(Offset.Zero) }
            var cardSizePx by remember { mutableStateOf(IntSize(0, 0)) }

            Box(
                //region DRAG INTERACTIONS
                modifier = Modifier
                    .clickable(onClick = { product2Modify = product })
                    .onGloballyPositioned { coords ->
                        cardTopLeftPx = coords.positionInRoot()
                        cardSizePx = coords.size
                    }
                    .pointerInput(product.databaseID) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { startOffsetInCard ->
                                isDragging = true
                                draggedProduct = product
                                touchOffsetWithinCard = startOffsetInCard
                                dragTopLeftPx = cardTopLeftPx + startOffsetInCard  // FIXED: Start at finger position
                                draggedCardSize = cardSizePx
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragTopLeftPx += dragAmount

                                // Calculate card center for trash hover detection
                                val overlayTopLeft = dragTopLeftPx - touchOffsetWithinCard
                                val center = overlayTopLeft + Offset(
                                    draggedCardSize.width / 2f,
                                    draggedCardSize.height / 2f
                                )

                                // Check both expanded bounds and distance-based detection
                                val expanded = trashRectBoundsPx?.let { it.inflate(24f) }
                                var nowHover = expanded?.contains(center) == true

                                // Also check distance-based detection (same as drop logic)
                                if (!nowHover && trashRectBoundsPx != null) {
                                    val trashCenter = Offset(
                                        (trashRectBoundsPx!!.left + trashRectBoundsPx!!.right) / 2f,
                                        (trashRectBoundsPx!!.top + trashRectBoundsPx!!.bottom) / 2f
                                    )
                                    val trashRadius = max(
                                        trashRectBoundsPx!!.width,
                                        trashRectBoundsPx!!.height
                                    ) / 2f
                                    val cardRadius = kotlin.math.hypot(
                                        draggedCardSize.width.toFloat(),
                                        draggedCardSize.height.toFloat()
                                    ) / 2f
                                    val gracePx = with(density) { 20.dp.toPx() }
                                    val distance = (center - trashCenter).getDistance()
                                    nowHover = distance <= (trashRadius + cardRadius + gracePx)
                                }

                                if (nowHover && !hoverOverTrash) {
                                    //Entering trash
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                                hoverOverTrash = nowHover
                            },
                            onDragEnd = {
                                val overlayTopLeft = dragTopLeftPx - touchOffsetWithinCard
                                val center = overlayTopLeft + Offset(
                                    draggedCardSize.width / 2f,
                                    draggedCardSize.height / 2f
                                )
                                val expanded = trashRectBoundsPx?.let { it.inflate(24f) }
                                var droppedInTrash = expanded?.contains(center) == true

                                if (!droppedInTrash && trashRectBoundsPx != null) {
                                    val trashCenter = Offset(
                                        (trashRectBoundsPx!!.left + trashRectBoundsPx!!.right) / 2f,
                                        (trashRectBoundsPx!!.top + trashRectBoundsPx!!.bottom) / 2f
                                    )


                                    val trashRadius = max(
                                        trashRectBoundsPx!!.width,
                                        trashRectBoundsPx!!.height
                                    ) / 2f
                                    val cardRadius = kotlin.math.hypot(
                                        draggedCardSize.width.toFloat(),
                                        draggedCardSize.height.toFloat()
                                    ) / 2f
                                    val gracePx = with(density) { 20.dp.toPx() }
                                    val distance = (center - trashCenter).getDistance()
                                    droppedInTrash =
                                        distance <= (trashRadius + cardRadius + gracePx)
                                }

                                if (droppedInTrash && draggedProduct != null) {
                                    val product2Delete = draggedProduct!!
                                    isPerformingDeleteAnimation = true

                                    coroutineScope.launch {
                                        val animation = Animatable(1f)
                                        animation.animateTo(
                                            targetValue = 0f,
                                            animationSpec = tween(durationMillis = 250)
                                        )
                                        //After animation we delete product and reset state
                                        onDeleteProduct(product2Delete)
                                        isPerformingDeleteAnimation = false
                                        isDragging = false
                                        draggedProduct = null
                                        dragTopLeftPx = Offset.Zero
                                        touchOffsetWithinCard = Offset.Zero
                                        draggedCardSize = IntSize(0, 0)
                                        hoverOverTrash = false
                                    }
                                } else {
                                    //Not over trash, reset state
                                    isDragging = false
                                    draggedProduct = null
                                    dragTopLeftPx = Offset.Zero
                                    touchOffsetWithinCard = Offset.Zero
                                    draggedCardSize = IntSize(0, 0)
                                    hoverOverTrash = false
                                }
                            },
                            onDragCancel = {
                                isDragging = false
                                draggedProduct = null
                                dragTopLeftPx = Offset.Zero
                                touchOffsetWithinCard = Offset.Zero
                                draggedCardSize = IntSize(0, 0)
                                hoverOverTrash = false
                            }
                        )
                    }
                    .zIndex(if (isDragging && draggedProduct?.databaseID == product.databaseID) 0f else 0f) //Makes sure overlay doesnt get clicks when overlay active
            )
            //endregion
            {
                if (isDragging && draggedProduct?.databaseID == product.databaseID) {
                    //Placeholder spacer when dragging
                    Spacer(
                        modifier = Modifier
                            .height(with(density) { cardSizePx.height.toDp() })
                            .fillMaxWidth()
                    )
                } else {
                    val productError = errors?.productErrors?.entries?.firstOrNull {
                        it.key.isEqualTo(product)
                    }?.value

                    ProductRow(
                        productName = product.name,
                        productPrice = formatFloatToDanishCurrency(product.price) + "kr",
                        error = productError,
                        onClick = { product2Modify = product }
                    )
                }
            }
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Tip: Hold inde på produkterne for at slette dem og tryk for at redigere",
                    fontStyle = FontStyle.Italic,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }
        }
    }

    //region DIALOGS
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
    //endregion

    //region TRASHCAN UI
    if (isDragging) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .onGloballyPositioned { coords ->
                        val topLeft = coords.positionInRoot()
                        val size = coords.size
                        trashRectBoundsPx =
                            Rect(topLeft, Size(size.width.toFloat(), size.height.toFloat()))
                    }
                    .size(96.dp)
                    .zIndex(0.9f)
            ) {
                val targetScale = if (hoverOverTrash) 1.12f else 1f
                val scaleAnimation by animateFloatAsState(targetScale)
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.trashcan_icon),
                    contentDescription = "Slet produkt",
                    modifier = Modifier
                        .align(Alignment.Center)
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scaleAnimation
                            scaleY = scaleAnimation
                        }
                )
            }
        }
    }
    //endregion

    //region FLOATING PRODUCTROW OVERLAY
    if (isDragging && draggedProduct != null) {
        val overlayTopLeft = dragTopLeftPx - touchOffsetWithinCard  // FIXED: Calculate card position from finger position
        val overlayIntOffset = with(density) {
            IntOffset(overlayTopLeft.x.roundToInt(), overlayTopLeft.y.roundToInt())
        }

        //Blocks interaction beneath while dragging
        Box(
            modifier = modifier
                .fillMaxSize(0.5f)
                .semantics { disabled() }
                .pointerInput(Unit) { /* Intercepts pointers when draggin */ }
                .zIndex(1f)
        ) {
            val overlayScale = remember { mutableStateOf(1f) }
            if (!isPerformingDeleteAnimation) {
                Box(
                    modifier = Modifier
                        .offset { overlayIntOffset}
                        .size(
                            with(density) {
                                val w = if (draggedCardSize.width > 0) draggedCardSize.width.toDp() else 160.dp
                                val h = if (draggedCardSize.height > 0) draggedCardSize.height.toDp() else 120.dp
                                androidx.compose.ui.unit.DpSize(w, h)
                            }
                        )
                        .graphicsLayer {
                            scaleX = overlayScale.value
                            scaleY = overlayScale.value
                        }
                ) {
                    ProductRow(
                        productName = draggedProduct!!.name,
                        productPrice = formatFloatToDanishCurrency(draggedProduct!!.price),
                        onClick = { /* NO-OP When dragging */ }
                    )
                }
            } else {
                //Deletion animation
                val scaleAnim = remember { Animatable(1f) }
                val alphaAnim = remember { Animatable(1f) }
                LaunchedEffect(draggedProduct?.databaseID) {
                    scaleAnim.animateTo(0f, animationSpec = tween(250))
                    alphaAnim.animateTo(0f, animationSpec = tween(250))
                }
                Box(
                    modifier = Modifier
                        .offset { overlayIntOffset }
                        .size(
                            with(density) {
                                val w = if (draggedCardSize.width > 0) draggedCardSize.width.toDp() else 160.dp
                                val h = if (draggedCardSize.height > 0) draggedCardSize.height.toDp() else 120.dp
                                androidx.compose.ui.unit.DpSize(w, h)
                            }
                        )
                        .graphicsLayer {
                            scaleX = scaleAnim.value
                            scaleY = scaleAnim.value
                            alpha = alphaAnim.value
                            shadowElevation = 8.dp.toPx()
                        }
                ) {
                    ProductCard(
                        name = draggedProduct!!.name,
                        price = draggedProduct!!.price,
                        isFavorite = draggedProduct!!.isFavorite,
                        onFavoriteClick = { }
                    )
                }
            }
        }
    }
    //endregion
}