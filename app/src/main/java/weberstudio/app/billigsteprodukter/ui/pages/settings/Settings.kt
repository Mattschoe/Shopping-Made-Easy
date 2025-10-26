package weberstudio.app.billigsteprodukter.ui.pages.settings

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
import androidx.compose.ui.unit.dp
import weberstudio.app.billigsteprodukter.data.settings.Theme

/**
 * The UI content of the *Settings* Page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPageContent(modifier: Modifier = Modifier, viewModel: SettingsViewModel) {
    var themeChooseExpanded by remember { mutableStateOf(false) }
    val theme by viewModel.theme.collectAsState()

    Column (
        modifier = modifier
            .padding(12.dp)
            .fillMaxSize()
    ) {
        SectionDivider("Tema")
        //Light/Dark theme
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
                        .width(200.dp)
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

        Spacer(Modifier.height(24.dp))
        SectionDivider("Kvitteringscanning")
    }
}

@Composable
fun SectionDivider(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
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
        text = text
    )
}