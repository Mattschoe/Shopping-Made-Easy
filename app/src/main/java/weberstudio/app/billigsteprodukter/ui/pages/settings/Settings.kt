package weberstudio.app.billigsteprodukter.ui.pages.settings

import android.util.Log
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.data.settings.Coop365Option
import weberstudio.app.billigsteprodukter.data.settings.Theme
import weberstudio.app.billigsteprodukter.ui.components.Coop365OptionDialog

/**
 * The UI content of the *Settings* Page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageContent(modifier: Modifier = Modifier, viewModel: SettingsViewModel) {
    //UI
    val buttonWidth = 200.dp
    val buttonHeight = 56.dp

    //THEME
    var themeChooseExpanded by remember { mutableStateOf(false) }
    val theme by viewModel.theme.collectAsState()

    //RECEIPT
    var onChangeCoopReceipt by remember { mutableStateOf(false) }

    //DATABASE

    Column (
        modifier = modifier
            .padding(12.dp)
            .fillMaxSize()
    ) {
        //region THEME
        SectionDivider("Tema")
        //region Light/Dark theme
        Row (
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            SettingsText("Mørk/Lys tilstand")
            ExposedDropdownMenuBox(
                expanded = themeChooseExpanded,
                onExpandedChange = { themeChooseExpanded = it },
                modifier = Modifier
                    .wrapContentWidth()
            ) {
                OutlinedTextField(
                    value = theme.toString(),
                    onValueChange = { /* READ ONLY */ },
                    readOnly = true,
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = themeChooseExpanded)
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .menuAnchor()
                        .width(buttonWidth)
                        .height(buttonHeight)
                )
                ExposedDropdownMenu(
                    expanded = themeChooseExpanded,
                    onDismissRequest = { themeChooseExpanded = false },
                ) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "System standard"
                            )
                        },
                        onClick = {
                            viewModel.setTheme(Theme.SYSTEM)
                            themeChooseExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Mørk tilstand"
                            )
                        },
                        onClick = {
                            viewModel.setTheme(Theme.DARK)
                            themeChooseExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "Lys tilstand"
                            )
                        },
                        onClick = {
                            viewModel.setTheme(Theme.LIGHT)
                            themeChooseExpanded = false
                        }
                    )
                }
            }
        }
        //endregion
        //endregion

        Spacer(Modifier.height(24.dp))

        //region RECEIPT SETTINGS
        SectionDivider("Kvitteringscanning")
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
    }

    //region DIALOGS
    if (onChangeCoopReceipt) {
        Coop365OptionDialog(
            title = "Skift kvitteringstype for Coop365",
            options = listOf(
                Coop365Option(Coop365Option.Option.OVER, R.drawable.exclamation_icon),
                Coop365Option(Coop365Option.Option.UNDER, R.drawable.exclamation_icon)
            ),
            onDismiss = { onChangeCoopReceipt = false },
            onConfirm = {
                viewModel.setCoop365Option(it)
                onChangeCoopReceipt = false
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
fun SettingsButton(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton (
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.primary,

        )
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onPrimary,
        )
    }
}

