package weberstudio.app.billigsteprodukter.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.data.Product
import weberstudio.app.billigsteprodukter.logic.Formatter.formatFloatToDanishCurrency
import weberstudio.app.billigsteprodukter.logic.Formatter.formatInputToDanishCurrencyStandard
import weberstudio.app.billigsteprodukter.logic.Store

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
fun ModifyTotalDialog(showDialog: Boolean, originalTotal: Float, hasTotalError: Boolean = false, onDismiss: () -> Unit, onConfirm: (Float) -> Unit) {
    if (!showDialog) return


    var newTotal by remember { mutableStateOf("") }
    val isValid = newTotal.toFloatOrNull() != null

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                //Header
                Text(
                    text = "Opdater totalpris",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                //Pris på produkt
                OutlinedTextField(
                    value = newTotal,
                    onValueChange = { input ->
                        val formatted = input.replace(',', '.')
                        newTotal = formatted
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = {
                        Text(
                            text = formatFloatToDanishCurrency(originalTotal),
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Displayer error, hvis den er der, ellers bare en warning
                if (hasTotalError) {
                    Text(
                        text = "Vi mistænker scanneren ikke har aflæst totalprisen korrekt, dobbeltcheck venligst fra total fra kvitteringen",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                } else {
                    //Warning
                    Text(
                        text = "Advarsel! Sletter eller ændres prisen på et produkt fra kvitteringen vil dette opdatere prisen på kvitteringen igen",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                //Done button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            val total = newTotal.toFloat()
                            onConfirm(total)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        ),
                        enabled = isValid
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.check_icon),
                            contentDescription = "Færdig",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

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


/**
 * Updates a product given user input, and returns it via [onConfirm]
 */
@Composable
fun ModifyProductDialog(product: Product, onDismiss: () -> Unit, onConfirm: (Product) -> Unit) {
    var newName by remember { mutableStateOf(product.name) }
    var newPrice by remember { mutableStateOf("") }
    val isValid = newName.trim().isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                //Header
                Text(
                    text = "Opdater produkt",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                //Navn på produkt
                OutlinedTextField(
                    value = newName,
                    onValueChange = { input ->
                        newName = input
                    },
                    placeholder = { Text("Navn...") },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Pris på produkt
                OutlinedTextField(
                    value = newPrice,
                    onValueChange = { input ->
                        val formatted = input.replace(',', '.')
                        newPrice = formatted
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = {
                        Text(
                            text = formatFloatToDanishCurrency(product.price),
                            color = Color.Gray,
                            fontStyle = FontStyle.Italic
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Done button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            val price: Float? = newPrice.toFloatOrNull()
                            onConfirm(product.copy(name = newName, price = if (price != null) price else product.price))
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp)),
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        ),
                        enabled = isValid
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.check_icon),
                            contentDescription = "Færdig",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Maps a raw cursor position to the formatted string position
 * @param raw: the exact string the user typed (may contain grouping dots)
 * @param formatted: the output from [formatInputToDanishCurrencyStandard]
 * @param rawCursor: the selection start in raw string
 */
fun mapCursorRawToFormatted(raw: String, formatted: String, rawCursor: Int): Int {
    // Count how many digits are *before* rawCursor in the raw input
    val digitsBefore = raw.substring(0, rawCursor.coerceIn(0, raw.length)).count { it.isDigit() }
    if (digitsBefore == 0) return 0

    // Find position in formatted where we've seen digitsBefore digits
    var digitsSeen = 0
    for (i in formatted.indices) {
        if (formatted[i].isDigit()) digitsSeen++
        if (digitsSeen >= digitsBefore) return (i + 1).coerceIn(0, formatted.length)
    }
    return formatted.length
}
