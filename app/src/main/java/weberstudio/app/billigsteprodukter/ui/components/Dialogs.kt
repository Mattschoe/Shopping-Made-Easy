package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import weberstudio.app.billigsteprodukter.logic.Store

private data class DialogUI(
    val shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    val focusedContainerColor: Color = Color(0xFFDFFFD6),
    val unfocusedContainerColor: Color = Color(0xFFDFFFD6)
)


@Composable
fun AddProductToListDialog(showDialog: Boolean, onDismiss: () -> Unit, onConfirm: (name: String, store: Store) -> Unit) {
    if (!showDialog) return

    //Local UI state
    var productName by rememberSaveable { mutableStateOf("") }
    var selectedStore by rememberSaveable { mutableStateOf<Store?>(null) }
    var expanded by remember { mutableStateOf(false) } //Is DropDown expanded
    var dialogUI = remember { DialogUI() }
    val isValid = productName.trim().isNotEmpty() && selectedStore != null //Form validity

    // Build the TextFieldColors from DialogUI inside a @Composable scope
    val textFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = dialogUI.focusedContainerColor,
        unfocusedContainerColor = dialogUI.unfocusedContainerColor,
    )

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = dialogUI.shape,
            shadowElevation = 6.dp,
            modifier = Modifier
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .widthIn(min = 280.dp, max = 380.dp)
            ) {
                Text(
                    text = "Tilføj til indkøbsliste",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Navn på produkt
                TextField(
                    value = productName,
                    onValueChange = { productName = it },
                    placeholder = { Text("Navn...") },
                    singleLine = true,
                    shape = dialogUI.shape,
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Store DropDown
                Box(
                    modifier = Modifier
                        .clickable { expanded = true }
                ) {
                    TextField(
                        value = selectedStore?.name ?: "Vælg butik..",
                        onValueChange = {},
                        enabled = false,
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Vælg butik",
                            )
                        },
                        singleLine = true,
                        shape = dialogUI.shape,
                        colors = textFieldColors,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                            .heightIn(min = 36.dp)
                    )

                    //Store DropDown
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Store.entries.forEach { store ->
                            DropdownMenuItem(
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

                // Simple action row for confirm/dismiss (optional)
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
    val dialogUI = remember { DialogUI() }

    //UI
    val colors = TextFieldDefaults.colors (
        focusedContainerColor = dialogUI.focusedContainerColor,
        unfocusedContainerColor = dialogUI.unfocusedContainerColor
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        title = { Text("Opret produkt") },
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
                    shape = dialogUI.shape,
                    colors = colors,
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
                        shape = dialogUI.shape,
                        colors = colors,
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
                            onValueChange = { /* READ ONLY */},
                            readOnly = true,
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            shape = dialogUI.shape,
                            colors = colors,
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
            TextButton(onClick = {
                val price = productPriceText.toFloatOrNull() ?: return@TextButton
                val store = selectedStore ?: return@TextButton
                onConfirm(productName.trim(), price, store)
            }) {
                Text("Tilføj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuller")
            }
        }
    )
}