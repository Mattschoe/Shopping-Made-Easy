package weberstudio.app.billigsteprodukter.ui.pages.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import weberstudio.app.billigsteprodukter.data.AdsID
import weberstudio.app.billigsteprodukter.data.settings.Theme
import weberstudio.app.billigsteprodukter.data.settings.TotalOption
import weberstudio.app.billigsteprodukter.ui.components.Coop365OptionDialog
import weberstudio.app.billigsteprodukter.ui.components.DeleteConfirmationDialog
import weberstudio.app.billigsteprodukter.ui.components.LargeBannerAd

/**
 * The UI content of the *Settings* Page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageContent(modifier: Modifier = Modifier, viewModel: SettingsViewModel) {
    val uriHandler = LocalUriHandler.current

    //UI
    val buttonWidth = 180.dp
    val buttonHeight = 56.dp

    //THEME
    val theme by viewModel.theme.collectAsState()
    val themeOptions = listOf(
        DropdownOption(Theme.SYSTEM, Theme.SYSTEM.toString()),
        DropdownOption(Theme.DARK, Theme.DARK.toString()),
        DropdownOption(Theme.LIGHT, Theme.LIGHT.toString())
    )

    //RECEIPT
    val totalOption by viewModel.totalOption.collectAsState()
    val totalOptions = listOf(
        DropdownOption(TotalOption.RECEIPT_TOTAL, TotalOption.RECEIPT_TOTAL.toString()),
        DropdownOption(TotalOption.PRODUCT_TOTAL, TotalOption.PRODUCT_TOTAL.toString())
    )
    var onChangeCoopReceipt by remember { mutableStateOf(false) }
    var showClearDatabaseWarning by remember { mutableStateOf(false) }

    //DATABASE

    Column (
        modifier = modifier
            .padding(12.dp)
            .fillMaxSize()
    ) {
        //region THEME
        SectionDivider("Tema")
        SettingsDropdownRow(
            label = "Mørk/Lys tilstand",
            selectedValue = theme,
            options = themeOptions,
            buttonWidth = buttonWidth,
            buttonHeight = buttonHeight,
            onOptionSelected = { viewModel.setTheme(it) }
        )
        //endregion

        Spacer(Modifier.height(24.dp))

        //region RECEIPT SETTINGS
        SectionDivider("Kvitteringscanning")
        SettingsDropdownRow(
            label = "Vælg hvordan totalpris beregnes",
            selectedValue = totalOption,
            options = totalOptions,
            buttonWidth = buttonWidth,
            buttonHeight = buttonHeight,
            onOptionSelected = { viewModel.setTotalOption(it) }
        )

        Spacer(Modifier.height(12.dp))

        //region Coop kvitteringstype
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SettingsText(
                text = "Coop kvittering type",
                modifier = Modifier.weight(1f)
            )
            SettingsButton(
                title = "Skift",
                onClick = { onChangeCoopReceipt = true },
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight)
            )
        }
        //endregion

        Spacer(Modifier.height(12.dp))

        //region RYD DATABASE
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SettingsText(
                text = "Ryd database",
                modifier = Modifier.weight(1f)
            )
            SettingsButton(
                title = "Ryd",
                onClick = { showClearDatabaseWarning = true },
                modifier = Modifier
                    .width(buttonWidth)
                    .height(buttonHeight),
                dangerous = true
            )
        }
        //endregion

        Spacer(Modifier.height(12.dp))

        //region CONTACT
        SectionDivider("Kontakt")
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SettingsText(
                text = "Har du noget feedback, oplevet stødende indhold eller andet? Kontakt os på:"
            )
            Text(
                text = "shoppingmadeeasy@creategoodthings.dk",
                color = Color.Blue,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { uriHandler.openUri("mailto:shoppingmadeeasy@creategoodthings.dk") }
            )
        }
        //endregion

        Spacer(Modifier.weight(1f))
    }

    //region DIALOGS
    if (onChangeCoopReceipt) {
        Coop365OptionDialog(
            onDismiss = { onChangeCoopReceipt = false },
            onConfirm = {
                viewModel.setCoop365Option(it)
                onChangeCoopReceipt = false
            }
        )
    }

    if (showClearDatabaseWarning) {
        DeleteConfirmationDialog(
            title = "Slet database?",
            body = "Denne handling vil slette alle produkter der hidtil er blevet scannet igennem kvittering, samt produkter manuelt tilføjet. Denne handling vil også slette alle produkter tilføjet i indkøbslister. Dette kan IKKE fortrydes",
            onDismiss = { showClearDatabaseWarning = false },
            onConfirm = {
                viewModel.deleteDatabase()
                showClearDatabaseWarning = false
            }
        )
    }
    //endregion
}

@Composable
fun SectionDivider(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            color = Color.Gray.copy(alpha = 0.3f),
            thickness = 1.dp
        )

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.Gray
        )

        HorizontalDivider(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
            color = Color.Gray.copy(alpha = 0.3f),
            thickness = 1.dp
        )
    }
}

@Composable
fun SettingsText(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        maxLines = 2
    )
}

@Composable
fun SettingsButton(title: String, onClick: () -> Unit, modifier: Modifier = Modifier, dangerous: Boolean = false) {
    Button(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (!dangerous) MaterialTheme.colorScheme.primary else Color.Red,
        )
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> SettingsDropdownRow(
    label: String,
    selectedValue: T,
    options: List<DropdownOption<T>>,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
    buttonWidth: Dp,
    buttonHeight: Dp,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier.fillMaxWidth()
    ) {
        SettingsText(label, Modifier.weight(1f).padding(end = 2.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.wrapContentWidth()
        ) {
            OutlinedTextField(
                value = selectedValue.toString(),
                onValueChange = { /* READ ONLY */ },
                readOnly = true,
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                textStyle = TextStyle(fontSize = 14.sp),
                modifier = Modifier
                    .menuAnchor()
                    .width(buttonWidth)
                    .height(buttonHeight)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(text = option.displayText) },
                        onClick = {
                            onOptionSelected(option.value)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

data class DropdownOption<T>(
    val value: T,
    val displayText: String
)

