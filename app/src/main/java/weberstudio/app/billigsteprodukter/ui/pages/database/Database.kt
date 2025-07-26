package weberstudio.app.billigsteprodukter.ui.pages.database

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.logic.Store

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseContent(
    modifier: Modifier = Modifier,
    viewModel: DataBaseViewModel,
    currentStore: Store
) {
    //TODO: Det her skal ændres siden det bare er alfabetisk rækkefølge, bliver nødt til at finde en måde at sortere dem ud efter dem som har mest data eller lade useren "hjerte" deres yndlingsstore

    //region Loads the products by checking when the LazyRow updates
    val stores = remember { Store.entries.toList() }
    val currentStoreProducts by viewModel.getProductsFromCurrentStore().collectAsState()
    val rowState = rememberLazyListState()
    LaunchedEffect(rowState) {
        snapshotFlow { rowState.firstVisibleItemIndex }
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
        //Store logo
        LazyRow(
            state = rowState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            itemsIndexed(stores) { index, store ->
                Image(
                    modifier = Modifier
                        .height(205.dp)
                        .padding(end = 8.dp)
                        .border( //Highlights the currently centered store
                            width = if (rowState.firstVisibleItemIndex == index) 3.dp else 0.dp,
                            color = if (rowState.firstVisibleItemIndex == index) Color.Blue else Color.Transparent,
                            shape = RoundedCornerShape(8.dp)
                        ),
                    painter = painterResource(id = store.image),
                    contentDescription = "Logo for ${store.name}",
                    contentScale = ContentScale.Crop
                )
            }
        }

        //Search
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            //Indsæt search her

            Spacer(modifier = Modifier.width((8.dp)))

            //Indsæt search scope dropdown her
        }

        //Product list/grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(currentStoreProducts) { product ->
                ProductCard(product)
            }
        }
    }
}