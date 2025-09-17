package weberstudio.app.billigsteprodukter.ui.components

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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import weberstudio.app.billigsteprodukter.logic.Store
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation.ShoppingList

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

@Composable
fun AddShoppingListDialog(showDialog: Boolean, onDismiss: () -> Unit, onConfirm: (name: String) -> Unit) {
    if (!showDialog) return

    //Local UI state
    var shoppingListName by rememberSaveable { mutableStateOf("") }
    var dialogUI = remember { DialogUI() }

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
                    text = "Giv din indkøbsliste et navn!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Navn på produkt
                TextField(
                    value = shoppingListName,
                    onValueChange = { shoppingListName = it },
                    placeholder = { Text("Navn...") },
                    singleLine = true,
                    shape = dialogUI.shape,
                    colors = textFieldColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 44.dp)
                )
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
                    Button(onClick = { onConfirm(shoppingListName) }) {
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

/**
 * //TODO: STANDARDIZE THE UI IN THIS
 */
@Composable
fun AddListDialog(onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var listName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White // TODO: Replace with theme color
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Tilføj ny indkøbsliste",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black // TODO: Replace with theme color
                )

                OutlinedTextField(
                    value = listName,
                    onValueChange = { listName = it },
                    label = { Text("Navn på liste") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
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
                            color = Color.Gray // TODO: Replace with theme color
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (listName.isNotBlank()) {
                                onConfirm(listName.trim())
                            }
                        },
                        enabled = listName.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50) // TODO: Replace with theme color
                        )
                    ) {
                        Text("Tilføj")
                    }
                }
            }
        }
    }
}

/**
 * //TODO: STANDARDIZE THE UI IN THIS
 */
@Composable
fun DeleteConfirmationDialog(shoppingList: ShoppingList, onDismiss: () -> Unit, onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White // TODO: Replace with theme color
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Slet indkøbsliste?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black // TODO: Replace with theme color
                )

                Text(
                    text = "JEG ER ØDELAGT FIX MIG",
                    fontSize = 16.sp,
                    color = Color.Gray // TODO: Replace with theme color
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
                            color = Color.Gray // TODO: Replace with theme color
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red // TODO: Replace with theme color
                        )
                    ) {
                        Text("Slet")
                    }
                }
            }
        }
    }
}