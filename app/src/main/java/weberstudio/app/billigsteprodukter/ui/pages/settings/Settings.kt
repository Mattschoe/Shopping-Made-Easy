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

/**
 * En swipable dialog der lader useren vælge imellem de forskellige options for kvitteringstype Coop365 har
 */
@Composable
fun Coop365OptionDialog(
    modifier: Modifier = Modifier,
    title: String,
    options: List<Coop365Option>,
    initialSelection: Int = 0,
    onDismiss: () -> Unit,
    onConfirm: (Coop365Option.Option) -> Unit,
) {
    val pagerState = rememberPagerState(
        initialPage = initialSelection,
        pageCount = { options.size + 1 }
    )

    val option = options.elementAtOrNull(pagerState.currentPage - 1)
    val isValid = option != null

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
            ) {
                //TITEL
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(12.dp))

                //OPTIONS
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                ) { page ->
                    if (page == 0) {
                        //region Forklarer problemet
                        Card(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {

                                Text(
                                    text = "Coop365 kvittering kommer i 2 forskellige formater.",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )

                                Text(
                                    text = "Enten har den mængden af et hvis købt produkt over produktnavnet, eller så er det under produktnavnet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Vælg venligst ud fra de to muligheder hvordan din butiks kvitteringer ser ud",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        //endregion
                    } else {
                        Coop365OptionDisplay(option = options[page - 1])
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                //DOTS
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    PagerIndicator(
                        pageCount = options.size + 1, //So intro page is included
                        currentPage = pagerState.currentPage,
                        currentPageOffsetFraction = pagerState.currentPageOffsetFraction,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                //region OK/ANNULLER
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
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
                    Button(
                        enabled = isValid,
                        onClick = { option?.let { onConfirm(it.type) } }
                    ) {
                        Text(
                            text = "Ok",
                            color = if (isValid) MaterialTheme.colorScheme.onPrimary else Color.Gray
                        )
                    }
                }
                //endregion
            }
        }
    }
}




@Composable
fun Coop365OptionDisplay(
    option: Coop365Option,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            //Title
            Text(
                text = option.type.toString(),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            //Image
            Image(
                imageVector = ImageVector.vectorResource(option.imageVector),
                contentDescription = null,
            )
        }
    }
}

@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier,
    currentPageOffsetFraction: Float = 0f
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { page ->
            val isSelected = page == currentPage
            val isNextSelected = page == currentPage + 1

            // Calculate width based on scroll offset
            val width by animateDpAsState(
                targetValue = when {
                    isSelected -> 24.dp - (currentPageOffsetFraction * 16).dp
                    isNextSelected -> 8.dp + (currentPageOffsetFraction * 16).dp
                    else -> 8.dp
                },
                animationSpec = tween(durationMillis = 300),
                label = "dot_width"
            )

            // Calculate alpha based on scroll offset
            val alpha = when {
                isSelected -> 1f - (currentPageOffsetFraction * 0.7f)
                isNextSelected -> 0.3f + (currentPageOffsetFraction * 0.7f)
                else -> 0.3f
            }

            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

