package weberstudio.app.billigsteprodukter.ui.pages.database

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.ui.components.ProductCard
import weberstudio.app.billigsteprodukter.ui.components.ProductCardSkeleton
import weberstudio.app.billigsteprodukter.ui.components.SearchBar
import weberstudio.app.billigsteprodukter.ui.components.StoreScopeDropDownMenu

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
    val isLoading by viewModel.isCurrentStoreLoading.collectAsState()

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
        BoxWithConstraints(
            modifier = Modifier
                .height(150.dp)
        ) {
            val pageInset: Dp = maxWidth * 0.17f //The small "before" and "after" stores you can see in the pager
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = pageInset),
                pageSpacing = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    val store = stores[page]
                    Image(
                        painter = painterResource(id = store.image),
                        contentDescription = "Logo for ${store.name}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }


        //Search + Store filter
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

        //Product list/grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .weight(1f) //Takes the rest of the space. OBS: DONT USE WEIGHT ANYWHERE ELSE FOR THIS TO WORK
                .fillMaxSize()
                .padding(4.dp)
                .graphicsLayer {}
        ) {
            items(filteredAndRankedProducts) { product ->
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