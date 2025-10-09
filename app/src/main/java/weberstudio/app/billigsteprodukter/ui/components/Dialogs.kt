package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Store


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductToListDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, store: Store) -> Unit,
    searchResults: List<Product>,
    onSearchQueryChange: (String) -> Unit,
    onSelectExistingProduct: (Product) -> Unit
) {
    if (!showDialog) return

    //Local UI state
    var productName by rememberSaveable { mutableStateOf("") }
    var selectedStore by rememberSaveable { mutableStateOf<Store?>(null) }
    var expanded by remember { mutableStateOf(false) } //Is DropDown expanded
    var showSearchResults by remember { mutableStateOf(false) }
    val isValid = productName.trim().isNotEmpty() && selectedStore != null //Form validity

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .padding(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .widthIn(min = 280.dp, max = 380.dp)
            ) {
                Text(
                    text = "Tilføj til indkøbsliste",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Navn på produkt
                OutlinedTextField(
                    value = productName,
                    onValueChange = { query ->
                        productName = query
                        onSearchQueryChange(query)
                        showSearchResults = query.length >= 3 && searchResults.isNotEmpty()
                    },
                    placeholder = { Text("Navn...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Search result
                if (showSearchResults) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 200.dp) //Limit height so it doesn't take whole dialog
                            .background(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        items(searchResults) { product ->
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectExistingProduct(product)
                                        showSearchResults = false
                                        onDismiss()
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Column {
                                    Text(
                                        text = product.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${product.store.name} • ${product.price}kr",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    //Store DropDown
                    OutlinedTextField(
                        value = selectedStore?.name ?: "Vælg butik..",
                        onValueChange = { /* READ ONLY */ },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor =
                                if (selectedStore != null) MaterialTheme.colorScheme.secondaryContainer
                                else Color.Gray.copy(alpha = 0.5f),

                            unfocusedContainerColor =
                                if (selectedStore != null) MaterialTheme.colorScheme.secondaryContainer
                                else Color.Gray.copy(alpha = 0.5f),

                            focusedTextColor =
                                if (selectedStore != null) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor =
                                if (selectedStore != null) MaterialTheme.colorScheme.onSecondaryContainer
                                else MaterialTheme.colorScheme.onSurface,
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 36.dp)
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                    ) {
                        Store.entries.forEach { store ->
                            DropdownMenuItem (
                                text = {
                                    Text(
                                        text = store.name,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                onClick = {
                                    selectedStore = store
                                    expanded = false
                                }
                            )
                        }
                    }
                }


                Spacer(modifier = Modifier.height(12.dp))

                //Action row for confirm/dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) { Text("Annuller") }
                    Spacer(modifier = Modifier.width(8.dp))
                    // Confirm is enabled only when both fields are provided
                    Button(
                        onClick = {
                            // defensive check again
                            val trimmedName = productName.trim()
                            val store = selectedStore ?: return@Button
                            if (trimmedName.isNotEmpty()) onConfirm(trimmedName, store)
                        },
                        enabled = isValid
                    ) {
                        Text("Tilføj")
                    }
                }
            }
        }
    }
}

@Composable
fun AddShoppingListDialog(showDialog: Boolean, onDismiss: () -> Unit, onConfirm: (name: String) -> Unit) {
    if (!showDialog) return

    //Local UI state
    var shoppingListName by rememberSaveable { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 6.dp,
            modifier = Modifier
                .padding(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .widthIn(min = 280.dp, max = 380.dp)
            ) {
                Text(
                    text = "Giv din indkøbsliste et navn!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Navn på produkt
                OutlinedTextField(
                    value = shoppingListName,
                    onValueChange = { shoppingListName = it },
                    placeholder = { Text("Navn...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                //confirm/dismiss
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss,
                    ) {
                        Text(
                            text = "Annuller",
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { onConfirm(shoppingListName) }) {
                        Text(
                            text = "Tilføj",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }

}

/**
 * Prompts the user for product info which can be used to create a product
 * @param onConfirm the name and price given by user if they accept.
 * @param standardStore if you want to specifiy the standard store displayed on the store dropdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductDialog(showDialog: Boolean, onDismiss: () -> Unit, onConfirm: (name: String, price: Float, store: Store) -> Unit, standardStore: Store? = null) {
    if (!showDialog) return

    //Local UI state
    var productName by rememberSaveable { mutableStateOf("") }
    var productPriceText by rememberSaveable { mutableStateOf("") }
    var storeDropdownExpanded by remember { mutableStateOf(false) }
    var selectedStore by rememberSaveable { mutableStateOf<Store?>(standardStore) }
    var isValid: Boolean = productName.trim().isNotEmpty() && productPriceText.trim().isNotEmpty()


    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Opret produkt") },
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                //Name of product
                OutlinedTextField(
                    value = productName,
                    onValueChange = { productName = it },
                    placeholder = { Text("Navn...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,

                        focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                )


                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    //Price
                    OutlinedTextField(
                        value = productPriceText,
                        onValueChange = { productPriceText = it },
                        placeholder = { Text("Pris...") },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,

                            focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                    )

                    //Store dropdown
                    ExposedDropdownMenuBox(
                        expanded = storeDropdownExpanded,
                        onExpandedChange = { storeDropdownExpanded = !storeDropdownExpanded },
                        modifier = Modifier
                            .weight(2f)
                            .fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedStore?.name ?: "Vælg butik",
                            onValueChange = { /* READ ONLY */ },
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            shape = RoundedCornerShape(24.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer,

                                focusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = storeDropdownExpanded,
                            onDismissRequest = { storeDropdownExpanded = false },
                        ) {
                            Store.entries.forEach { store ->
                                DropdownMenuItem(
                                    onClick = {
                                        selectedStore = store
                                        storeDropdownExpanded = false
                                    },
                                    text = { Text(store.name) }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val price = productPriceText.toFloatOrNull() ?: return@TextButton
                    val store = selectedStore ?: return@TextButton
                    onConfirm(productName.trim(), price, store)
                },
                enabled = isValid
            ) {
                Text("Tilføj")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Annuller")
            }
        }
    )
}

@Composable
fun DeleteConfirmationDialog(title: String, body: String, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = body,
                    fontSize = 16.sp,
                    color = Color.Gray
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            "Annuller",
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Slet")
                    }
                }
            }
        }
    }
}