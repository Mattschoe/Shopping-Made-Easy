package weberstudio.app.billigsteprodukter.ui.pages.shoppingList

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.data.AdsID
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.data.ShoppingList
import weberstudio.app.billigsteprodukter.ui.components.BannerAd
import weberstudio.app.billigsteprodukter.ui.components.DeleteConfirmationDialog
import weberstudio.app.billigsteprodukter.ui.components.ReceiptTotalCard
import weberstudio.app.billigsteprodukter.ui.components.SearchBar
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListsPage(modifier: Modifier = Modifier, navController: NavController, viewModel: ShoppingListsViewModel) {
    val shoppingLists by viewModel.shoppingLists.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var shoppingListToDelete by remember { mutableStateOf<ShoppingList?>(null)}

    Column(
        modifier = modifier
    ) {
        //Shopping Lists
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            reverseLayout = true
        ) {
            items(shoppingLists) { shoppingList ->
                ShoppingListItem(
                    shoppingList = shoppingList,
                    onClick = { navController.navigate(PageNavigation.createShoppingListDetailRoute(shoppingList.ID)) },
                    onDeleteClick = {
                        shoppingListToDelete = shoppingList
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    //Delete Confirmation Dialog
    if (showDeleteDialog) {
        shoppingListToDelete?.let { list ->
            DeleteConfirmationDialog(
                title = "Slet indkøbsliste?",
                body = "Dette kan ikke fortrydes",
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    viewModel.deleteShoppingList(list)
                    showDeleteDialog = false
                }
            )
        }
    }
}

@Composable
fun ShoppingListItem(shoppingList: ShoppingList, onClick: () -> Unit, onDeleteClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                    color = Color.Black
                )
                Text(
                    text = "Oprettet den: ${DateTimeFormatter.ofPattern("dd/MM - yyyy").format(shoppingList.createdDate)}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Slet liste",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ShoppingListUndermenuContent(modifier: Modifier, viewModel: ShoppingListUndermenuViewModel) {
    val filteredList = viewModel.filteredStore2ProductsAdded2Store.collectAsState().value
    val isStoreExpanded by viewModel.isStoreExpanded.collectAsState()
    val storeTotals by viewModel.storeTotals.collectAsState()

    val shoppingListState by viewModel.selectedShoppingList.collectAsState()
    val shoppingList = shoppingListState
    if (shoppingList == null) {
        Box(
            modifier = modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val totalPrice by viewModel.priceTotal.collectAsState()
    val createdAtDate = remember { DateTimeFormatter.ofPattern("dd/MM").format(shoppingList.shoppingList.createdDate) }

    val listName = shoppingList.shoppingList.name
    var isEditingListName by remember { mutableStateOf(false) }
    var tempListName by remember(listName) { mutableStateOf(listName) }

    var hasInitialFocus by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .padding(12.dp)
    ) {
        //region TITLE
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            //Text
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                //Indkøbsliste navn
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEditingListName) {
                        OutlinedTextField(
                            value = tempListName,
                            onValueChange = { tempListName = it },
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    isEditingListName = false
                                    hasInitialFocus = false
                                    viewModel.updateShoppingListName(tempListName)
                                    focusManager.clearFocus()
                                }
                            ),
                            modifier = Modifier
                                .focusRequester(focusRequester)
                                .onFocusChanged { focusState ->
                                    if (focusState.isFocused) {
                                        hasInitialFocus = true
                                    } else if (hasInitialFocus) {
                                        isEditingListName = false
                                        hasInitialFocus = false
                                        viewModel.updateShoppingListName(tempListName)
                                    }
                                }
                        )
                        LaunchedEffect(Unit) {
                            delay(50)
                            focusRequester.requestFocus()
                        }
                    } else {
                        Text(
                            text = listName,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    isEditingListName = true
                                    hasInitialFocus = false
                                }
                        )
                    }
                    IconButton(
                        modifier = Modifier
                            .weight(0.3f),
                        onClick = {
                            if (isEditingListName) {
                                isEditingListName = false
                                hasInitialFocus = false
                                viewModel.updateShoppingListName(tempListName)
                                focusManager.clearFocus()
                            } else {
                                isEditingListName = true
                                hasInitialFocus = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector =
                                if (isEditingListName) Icons.Default.Check
                                else ImageVector.vectorResource( R.drawable.edit_icon),
                            contentDescription = if (isEditingListName) "Gem ændringer" else "Ændrer indkøbslistenavn",
                        )
                    }
                }
                Text(
                    text = "Oprettet den $createdAtDate",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                )
            }
        }
        //endregion

        //region SEARCH + TOTAL ROW
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                modifier = Modifier.weight(1f),
                searchQuery =  viewModel.listSearchQuery.collectAsState().value,
                onQueryChange =  viewModel::setListSearchQuery
            )
            ReceiptTotalCard(
                modifier = Modifier,
                totalPrice = BigDecimal(totalPrice).setScale(2, RoundingMode.HALF_EVEN).toString().replace(".", ",")
            )
        }
        //endregion

        //region SHOPPING LIST
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            filteredList.forEach { (store, productsWithStatus) ->
                val expanded = isStoreExpanded[store.ID] ?: true //Starter stores expanded (Don't follow IntelliSense here, it's lying) (goddamn CLANKER)
                val (total, checkedOff) = storeTotals[store] ?: Pair(0, 0)

                item(key = store.ID) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        //region Store Header
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleStore(store) }
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = store.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = if (expanded) "Collapse" else "Expand",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "$checkedOff/$total",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        //endregion

                        //Products
                        if (expanded) {
                            productsWithStatus.forEach { (product, isChecked) ->
                                ShoppingListProductCardUI(
                                    product = product,
                                    selected = isChecked,
                                    onToggle = { viewModel.toggleProduct(product) },
                                )
                                Spacer(Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }
        //endregion
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListProductCardUI(modifier: Modifier = Modifier, product: Product, selected: Boolean = false, onToggle: () -> Unit) {
    val backgroundColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
        animationSpec = tween(300)
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300)
    )

    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onToggle), //Gør hele produkt row klikbart
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
           Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.primary,
                    uncheckedColor = MaterialTheme.colorScheme.outline,
                    checkmarkColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )

            Spacer(modifier = Modifier.width(width = 12.dp))


            Column(modifier = Modifier.weight(1f)) {

                //Produktnavn
                Text(
                    text = product.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    textDecoration = if (selected) TextDecoration.LineThrough else TextDecoration.None
                )

                Spacer(modifier = Modifier.height(4.dp))

                //Produktdetaljer
                Text(
                    text = "${product.price}",
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f),
                    textDecoration = if (selected) TextDecoration.LineThrough else TextDecoration.None
                )

            }
        }
    }
}
