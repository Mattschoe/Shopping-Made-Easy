package weberstudio.app.billigsteprodukter.ui.pages.shoppingList

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.ui.components.AddListDialog
import weberstudio.app.billigsteprodukter.ui.components.DefaultProductCard
import weberstudio.app.billigsteprodukter.ui.components.DeleteConfirmationDialog
import weberstudio.app.billigsteprodukter.ui.components.ReceiptTotalCard
import weberstudio.app.billigsteprodukter.ui.components.SearchBar
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsPage(modifier: Modifier = Modifier, navController: NavController, onSortMenuClick: () -> Unit = {}, viewModel: ShoppingListsViewModel) {
    val shoppingLists by viewModel.shoppingLists.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<ShoppingList?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // TODO: Replace with theme color
    ) {
        //Shopping Lists
        LazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(shoppingLists) { shoppingList ->
                ShoppingListItem(
                    shoppingList = shoppingList,
                    onClick = { navController.navigate(PageNavigation.createShoppingListDetailRoute(shoppingList.ID)) },
                    onDeleteClick = { showDeleteDialog = shoppingList }
                )
            }
        }

        //Add Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF4CAF50), // TODO: Replace with theme color
                contentColor = Color.White,
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tilføj indkøbsliste"
                )
            }
        }
    }

    //Add List Dialog
    if (showAddDialog) {
        AddListDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { listName ->
                viewModel.addShoppingList(listName)
                showAddDialog = false
            }
        )
    }

    /*
    TODO: FIX
    //Delete Confirmation Dialog
    showDeleteDialog?.let { listToDelete ->
        DeleteConfirmationDialog(
            shoppingList = listToDelete,
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                viewModel.deleteShoppingList(listToDelete.ID)
                showDeleteDialog = null
            }
        )
    }

     */
}

@Composable
fun ShoppingListItem(shoppingList: ShoppingList, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8F5E8) // TODO: Replace with theme color
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = shoppingList.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black // TODO: Replace with theme color
                )
                Text(
                    text = "Oprettet den: ${shoppingList.createdDate}",
                    fontSize = 14.sp,
                    color = Color.Gray // TODO: Replace with theme color
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Slet liste",
                    tint = Color.Gray, // TODO: Replace with theme color
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ShoppingListUndermenuContent(modifier: Modifier, listID: String?, navController: NavController, viewModel: ShoppingListUndermenuViewModel) {
    //Loads the shopping list from ID given as param
    LaunchedEffect(listID) { //LaunchedEffect gør så koden bliver runned når listID ændrer sig
        if (listID != null) {
            viewModel.selectShoppingList(listID)
        }
    }

    val visibleStores by viewModel.store2ProductsAdded2Store.collectAsState()
    val isStoreExpanded by viewModel.isStoreExpanded.collectAsState()
    val selectedProducts by viewModel.selectedProducts.collectAsState()


    Column(
        modifier = modifier
            .border(2.dp, Color.Magenta)
    ) {
        //region TITLE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.Black)
        ) {
            //Text
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                //Indkøbsliste navn
                Row {
                    Text(
                        text = "Mit Indkøb",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(
                        modifier = Modifier
                            .size(24.dp),
                        onClick = {  } //Skift titlen på "Mit Indkøb"
                    ) {
                        Icon(imageVector = ImageVector.vectorResource(R.drawable.edit_icon), "Ændrer overskrift")
                    }
                }
                Text(
                    text = "Oprettet den 7/8",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                )
            }

            //Sort by icon
            IconButton(
                modifier = Modifier
                    .size(72.dp),
                onClick = { } //Sort order
            ) {
                Icon(imageVector = ImageVector.vectorResource(R.drawable.sortascending_icon), "Sorter efter")
            }
        }
        //endregion

        //region SEARCH + TOTAL ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color.Red)
        ) {
            SearchBar(Modifier.weight(1f), searchQuery =  "TEMP", onQueryChange =  {})
            ReceiptTotalCard(totalPrice = "193,95")
        }
        //endregion

        //region SHOPPING LIST
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, Color.Green)
        ) {
            visibleStores.forEach { store2ProductList ->
                val store = store2ProductList.key
                val expanded = isStoreExpanded[store.ID] == true
                val (total, checkedOff) = viewModel.getTotalAndCheckedOff(store)
                item(key = store.ID) {
                    StoreDropDown(
                        store = store,
                        isExpanded = isStoreExpanded[store.ID] == true,
                        onToggle = { viewModel.toggleStore(store, expanded) },
                        total = total,
                        checkedOff = checkedOff
                    )
                }

                if (expanded) {
                    items(
                        items = store2ProductList.value,
                        key = { product -> product.businessID }
                    ) { product ->
                        ShoppingListProductCardU(
                            product = product,
                            selected = selectedProducts.contains(product),
                            onToggle = { viewModel.toggleProduct(product) },
                        )
                    }
                }
            }
        }
        //endregion
    }
}

@Composable
fun StoreDropDown(modifier: Modifier = Modifier, store: Store, isExpanded: Boolean, onToggle: () -> Unit, total: Int, checkedOff: Int) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onToggle() }
            .animateContentSize(), // smooth size change when toggling
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = store.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text(text = if (isExpanded) "▲" else "▼")
            Text(text = "$checkedOff/$total", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun ShoppingListProductCardU(modifier: Modifier = Modifier, product: Product, selected: Boolean = false, onToggle: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) Color.Green else Color.LightGray
    )

    DefaultProductCard(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
            .clickable { onToggle() } //Gør hele produkt row klikbart
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() }
            )

            Spacer(modifier = Modifier.width(width = 12.dp))

            Column(modifier = Modifier.weight(1f)) {
                //Produktnavn
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(4.dp))

                //Produktdetaljer
                Text(
                    text = "${product.price} | 1kg", //TODO: Her skal prober enhed tilføjes når den funktionalitet er implementeret
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
