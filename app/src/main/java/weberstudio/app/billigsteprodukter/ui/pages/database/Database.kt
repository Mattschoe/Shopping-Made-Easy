package weberstudio.app.billigsteprodukter.ui.pages.database

import android.annotation.SuppressLint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.data.AdsID
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.ui.components.LargeBannerAd
import weberstudio.app.billigsteprodukter.ui.components.ModifyProductDialog
import weberstudio.app.billigsteprodukter.ui.components.PagerIndicator
import weberstudio.app.billigsteprodukter.ui.components.ProductCard
import weberstudio.app.billigsteprodukter.ui.components.SearchBar
import weberstudio.app.billigsteprodukter.ui.components.StoreScopeDropDownMenu
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.roundToInt

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseContent(
    modifier: Modifier,
    viewModel: DataBaseViewModel,
) {
    val stores = remember { Store.entries.toList() }
    val filteredAndRankedProducts by viewModel.filteredProductsFlow.collectAsState()
    val currentStore by viewModel.currentStore.collectAsState()
    val allStoresEnabled by viewModel.allStoresSearchEnabled.collectAsState()

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

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { stores.size })
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collectLatest { index ->
                stores.getOrNull(index)?.let { store ->
                    viewModel.selectStore(store)
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        //region STORE LOGO
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val pagerTotalHeight = maxHeight * 0.25f
            val pagerHeight = pagerTotalHeight - 30.dp //Space for indicators
            val horizontalContentPadding = maxWidth * 0.25f

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(pagerTotalHeight)
                    //.border(2.dp, Color.Black)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    HorizontalPager(
                        state = pagerState,
                        pageSize = PageSize.Fill,
                        contentPadding = PaddingValues(horizontal = horizontalContentPadding),
                        modifier = Modifier
                            .height(pagerHeight)
                            //.border(2.dp, Color.Magenta)
                    ) { page ->
                        //Page offset for animations
                        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
                        val absoluteOffset = pageOffset.absoluteValue

                        //Scale
                        val scale = lerp(1f, 0.85f, absoluteOffset.coerceIn(0f, 1f))

                        //Opacity
                        val alpha = when {
                            absoluteOffset < 1f -> lerp(1f, 0.6f, absoluteOffset)
                            else -> 0.3f
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                //.border(2.dp, Color.Blue)
                                    ,
                            contentAlignment = Alignment.Center
                        ) {
                            val store = stores[page]
                            Image(
                                painter = painterResource(id = store.image),
                                contentDescription = store.contentDescription,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .aspectRatio(1f)
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        this.alpha = alpha
                                        transformOrigin = TransformOrigin.Center
                                    }
                                    .clip(RoundedCornerShape(8.dp))
                                    //.border(2.dp, Color.Yellow)
                            )
                        }
                    }

                    PagerIndicator(
                        pageCount = stores.size,
                        currentPage = pagerState.currentPage,
                        currentPageOffsetFraction = pagerState.currentPageOffsetFraction,
                        modifier = Modifier
                            .padding(top = 4.dp,     bottom = 8.dp)
                    )
                }
            }
        }
        //endregion

        //region Search + Store filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                modifier = Modifier
                    .weight(1f, fill = false),
                searchQuery =  viewModel.searchQuery.collectAsState().value,
                onQueryChange =  viewModel::setSearchQuery
            )
            Spacer(modifier = Modifier.width((8.dp)))
            StoreScopeDropDownMenu(
                modifier = Modifier
                    .wrapContentWidth(),
                currentStore = currentStore,
                allStoresEnabled = allStoresEnabled,
                onAllStoresToggle = viewModel::setSearchAllStores
            )
        }
        //endregion

        //region PRODUCT GRID
        var product2Modify by remember { mutableStateOf<Product?>(null) }
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f) //Takes the rest of the space. OBS: DONT USE WEIGHT ANYWHERE ELSE FOR THIS TO WORK
                .fillMaxSize()
                .padding(4.dp)
        ) {
            items(
                items = filteredAndRankedProducts,
                key = { product -> product.databaseID }
            ) { product ->
                var cardTopLeftPx by remember { mutableStateOf(Offset.Zero) }
                var cardSizePx by remember { mutableStateOf(IntSize(0,0)) }

                Box(
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
                                    dragTopLeftPx = cardTopLeftPx
                                    touchOffsetWithinCard = startOffsetInCard
                                    draggedCardSize = cardSizePx
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    dragTopLeftPx += dragAmount
                                    val center = dragTopLeftPx + Offset(
                                        draggedCardSize.width / 2f,
                                        draggedCardSize.height / 2f
                                    )

                                    val expanded = trashRectBoundsPx?.let { it.inflate(24f) }
                                    val nowHover = expanded?.contains(center) == true
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


                                        val trashRadius = max(trashRectBoundsPx!!.width, trashRectBoundsPx!!.height) / 2f
                                        val cardRadius = kotlin.math.hypot(
                                            draggedCardSize.width.toFloat(),
                                            draggedCardSize.height.toFloat()
                                        ) / 2f
                                        val gracePx = with(density) { 20.dp.toPx() }
                                        val distance = (center - trashCenter).getDistance()
                                        droppedInTrash = distance <= (trashRadius + cardRadius + gracePx)
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
                                            viewModel.deleteProduct(product2Delete)
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
                ) {
                    if (isDragging && draggedProduct?.databaseID == product.databaseID) {
                        //Placeholder spacer when dragging
                        Spacer(
                            modifier = Modifier
                                .height(with(density) { cardSizePx.height.toDp() })
                                .fillMaxWidth()
                        )
                    } else {
                        ProductCard(
                            name = product.name,
                            price = product.price,
                            isFavorite = product.isFavorite,
                            onFavoriteClick = { viewModel.toggleFavorite(product) }
                        )
                    }
                }
            }
        }
        //endregion

        //region DIALOGS
        product2Modify?.let { product ->
            ModifyProductDialog(
                product = product,
                onDismiss = { product2Modify = null },
                onConfirm = { newProduct ->
                    viewModel.updateProduct(newProduct)
                    product2Modify = null
                }
            )
        }
        //endregion

    }

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

    //region FLOATING PRODUCTCARD OVERLAY
    if (isDragging && draggedProduct != null) {
        val overlayTopLeft = dragTopLeftPx - touchOffsetWithinCard
        val overlayIntOffset = with(density) {
            IntOffset(overlayTopLeft.x.roundToInt(), overlayTopLeft.y.roundToInt())
        }

        //Blocks interaction beneath while dragging
        Box(
            modifier = modifier
                .fillMaxSize()
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
                    ProductCard(
                        name = draggedProduct!!.name,
                        price = draggedProduct!!.price,
                        isFavorite = draggedProduct!!.isFavorite,
                        onFavoriteClick = { /* No-Op when dragging '*/ }
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

