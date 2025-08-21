package weberstudio.app.billigsteprodukter.ui.pages.database

import android.annotation.SuppressLint
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.ui.components.ProductCard
import weberstudio.app.billigsteprodukter.ui.components.SearchBar
import weberstudio.app.billigsteprodukter.ui.components.StoreScopeDropDownMenu

@SuppressLint("UnusedBoxWithConstraintsScope")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseContent(
    modifier: Modifier,
    viewModel: DataBaseViewModel,
) {
    //TODO: Det her skal ændres siden det bare er alfabetisk rækkefølge, bliver nødt til at finde en måde at sortere dem ud efter dem som har mest data eller lade useren "hjerte" deres yndlingsstore

    //region Loads the products by checking when the HorizontalPager updates
    val stores = remember { Store.entries.toList() }
    val filteredAndRankedProducts by viewModel.filteredProductsFlow().collectAsState()
    val currentStore by viewModel.currentStore.collectAsState()
    val allStoresEnabled by viewModel.allStoresSearchEnabled.collectAsState()
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { stores.size })
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged() //Avoids small scroll/jitters
            .collectLatest { index -> //Cancels old load and runs new load, prevents backlog
                stores.getOrNull(index)?.let { store ->
                    viewModel.selectStore(store)
                }
            }
    }
    //endregion

    //UI
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .height(150.dp) //Size of images (ish)
                //.border(4.dp, Color.Green)
        ) {
            val pageInset: Dp = maxWidth * 0.17f //The small "before" and "after" stores you can see in the pager
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = pageInset),
                pageSpacing = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                //.padding(horizontal = 16.dp)
                ,
            ) { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                        //.border(width = 3.dp, color = Color.Magenta)
                    contentAlignment = Alignment.Center
                ) {
                    val store = stores[page]
                    Image(
                        painter = painterResource(id = store.image),
                        contentDescription = "Logo for ${store.name}",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize() //Logo size
                            .wrapContentSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }
            }
        }


        //Search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                //.border(width = 4.dp, color = Color.Red)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(modifier = Modifier.weight(2f), searchQuery =  viewModel.searchQuery.collectAsState().value, onQueryChange =  viewModel::setSearchQuery )
            Spacer(modifier = Modifier.width((8.dp)))
            StoreScopeDropDownMenu(modifier = Modifier.weight(1.33f), currentStore = currentStore, allStoresEnabled = allStoresEnabled, onAllStoresToggle = viewModel::setSearchAllStores)
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
                //.border(width = 4.dp, color = Color.Black)
                .padding(4.dp)
        ) {
            items(filteredAndRankedProducts) { product ->
                ProductCard(
                    name = product.name,
                    price = product.price.toString(), //TODO Product skal være en string og ikke float
                    isFavorite = false,
                    onFavoriteClick = { viewModel.toggleFavorite(product) }
                )
            }
        }
    }
}