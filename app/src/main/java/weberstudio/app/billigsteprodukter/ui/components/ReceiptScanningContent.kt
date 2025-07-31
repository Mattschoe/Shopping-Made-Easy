package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.logic.Store
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import weberstudio.app.billigsteprodukter.logic.Product
import weberstudio.app.billigsteprodukter.ui.theme.ThemeLightGreen
import weberstudio.app.billigsteprodukter.ui.theme.ThemeLightGrey
import weberstudio.app.billigsteprodukter.ui.theme.ThemeTEMP

/**
 * @param productName the name of the product (Title)
 * @param productPrice the price of the product
 * @param onThreeDotMenuClick what should happen when the three dots are pressed
 */
@Composable
fun ProductRow(productName: String, productPrice: String, onThreeDotMenuClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeTEMP),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                   text = productName,
                    style = MaterialTheme.typography.bodyLarge, //ToDo: Change later to app font
                    color = ThemeLightGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = productPrice,
                    style = MaterialTheme.typography.bodyMedium, //ToDo: Change later to app font
                    color = ThemeLightGrey
                )
            }
            IconButton(
                onClick = onThreeDotMenuClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.threedots_icon),
                    modifier = Modifier
                        .rotate(90f),
                    contentDescription = "Indstillinger for $productName",
                    tint = ThemeTEMP
                )
            }
        }
    }
}

@Composable
fun AddProductToReceipt(modifier: Modifier = Modifier, addProductToReceipt: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = addProductToReceipt),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Green),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tilføj produkter til kvitteringen",
                modifier = Modifier
                    .size(48.dp)
            )
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

    //UI Settings
    val fieldsShape = RoundedCornerShape(24.dp)
    val fieldsColors = TextFieldDefaults.colors(
        focusedContainerColor = Color(0xFFDFFFD6),
        unfocusedContainerColor = Color(0xFFDFFFD6)
    )

    //UI
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
                    shape = fieldsShape,
                    colors = fieldsColors,
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
                        shape = fieldsShape,
                        colors = fieldsColors,
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
                            shape = fieldsShape,
                            colors = fieldsColors,
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