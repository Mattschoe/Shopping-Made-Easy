package weberstudio.app.billigsteprodukter.ui.pages.budget

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.ExtraExpense
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import weberstudio.app.billigsteprodukter.logic.Formatter.danishCurrencyToFloat
import weberstudio.app.billigsteprodukter.logic.Formatter.formatInputToDanishCurrency
import weberstudio.app.billigsteprodukter.logic.Formatter.toDanishString
import weberstudio.app.billigsteprodukter.logic.Formatter.filterInputToValidNumberInput
import weberstudio.app.billigsteprodukter.logic.Formatter.formatFloatToDanishCurrency
import weberstudio.app.billigsteprodukter.ui.components.DeleteConfirmationDialog
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.Year
import java.time.format.DateTimeFormatter


@Composable
fun BudgetPage(modifier: Modifier = Modifier, viewModel: BudgetViewModel, month: Month, year: Year) {
    val currentBudget by viewModel.currentBudget.collectAsState()
    val currentReceipts by viewModel.currentReceipts.collectAsState()
    val currentExpenses by viewModel.currentExtraExpenses.collectAsState()

    var selectedMonth by remember { mutableStateOf(month) }
    var selectedYear by remember { mutableStateOf(year) }
    viewModel.loadBudget(selectedMonth, selectedYear)

    //Hvis useren ikke har givet budgetInput for de intro pagen
    if (currentBudget == null) {
        PreBudgetPageUI(
            modifier = modifier,
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            onMonthSelected = { month, year ->
                selectedMonth = month
                selectedYear = year
                viewModel.loadBudget(month, year)
            },
            newBudget = { newBudget -> viewModel.addBudget(newBudget) }
        )
    }

    //Hvis useren har givet et budget så viser vi dem statistik pagen
    currentBudget?.let { currentBudget ->
        BudgetPageUI(
            modifier = modifier,
            currentBudget =  currentBudget.budget,
            totalSpent = (currentReceipts.sumOf { it.receipt.total.toDouble() } + currentExpenses.sumOf { it.price.toDouble() }).toFloat() ,
            receipts = currentReceipts,
            expenses = currentExpenses,
            selectedMonth = currentBudget.month,
            selectedYear = currentBudget.year,
            onAddExpense = { name, price -> viewModel.addExtraSpendingToCurrentBudget(name, price) },
            onMonthSelected = { month, year ->
                selectedMonth = month
                selectedYear = year
                viewModel.loadBudget(month, year)
            },
            onBudgetChanged = { newBudget ->
                viewModel.updateBudget(currentBudget.copy(budget = newBudget))
            },
            onDeleteReceipt = { receipt -> viewModel.deleteReceipt(receipt.receipt) },
            onDeleteExpense = { expense -> viewModel.deleteExpense(expense) }
        )
    }
}

//region UI
//region PreBudgetPageUI
/**
 * The UI for the user to input a monthly budget so we can navigate them to [BudgetPageUI]
 */
@Composable
fun PreBudgetPageUI(modifier: Modifier = Modifier, newBudget: (Budget) -> Unit, onMonthSelected: (Month, Year) -> Unit, selectedMonth: Month, selectedYear: Year) {
    var showCreateBudgetDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Hvad er budgettet på\nmad for ${selectedMonth.toDanishString()} måned?",
                    fontSize = 24.sp,
                    textAlign = TextAlign.Left,
                    lineHeight = 22.sp
                )

                Column(
                    modifier = Modifier
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(
                        onClick = { showCreateBudgetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Opret budget",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    Text(
                        text = "eller",
                        fontSize = 20.sp,
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )

                    //Month picker dropdown
                    Card(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .height(48.dp)
                            .clickable { showMonthPicker = true },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Se budget for andre måneder",
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.dropdown_icon),
                                contentDescription = "Vælg måned",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    //region DIALOGS<
    if (showCreateBudgetDialog) {
        BudgetDialog(
            selectedMonth = selectedMonth,
            onDismiss = { showCreateBudgetDialog = false },
            onClick = { budget ->
                showCreateBudgetDialog = false
                newBudget(budget)
            }
        )
    }


    if (showMonthPicker) {
        DatePickerDialog(
            currentMonth = null,
            currentYear = null,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { month, year ->
                onMonthSelected(month, year)
                showMonthPicker = false
            }
        )
    }
    //endregion
}

@Composable
fun BudgetDialog(selectedMonth: Month, onDismiss: () -> Unit, onClick: (Budget) -> Unit) {
    var totalBudget by remember { mutableStateOf(TextFieldValue("")) }
    val currentYear by remember { mutableStateOf(Year.from(LocalDateTime.now())) }
    val isValid = totalBudget.text.trim().isNotEmpty()

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
                    text = "Budget for ${selectedMonth.toDanishString()} $currentYear",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                //Total budget field
                BudgetInputField(
                    value = totalBudget,
                    onValueChange = { input ->
                        val validInput = filterInputToValidNumberInput(input.text)

                        if (!validInput.isEmpty()) {
                            val formatted = formatInputToDanishCurrency(validInput)
                            totalBudget = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        }
                    },
                    label = "I alt",
                    totalBudget = danishCurrencyToFloat(totalBudget.text),
                    currentAmount = danishCurrencyToFloat(totalBudget.text),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                //Done button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = {
                            val budget = Budget(
                                month = selectedMonth,
                                year = currentYear,
                                budget = danishCurrencyToFloat(totalBudget.text)
                            )
                            onClick(budget)
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
fun BudgetInputField(
    modifier: Modifier = Modifier,
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    totalBudget: Float,
    currentAmount: Float,
    showDelete: Boolean = false,
    onDelete: () -> Unit = {},
) {
    val percentage = if (totalBudget > 0) (currentAmount / totalBudget * 100) else 0.0

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedLabelColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            ),
            singleLine = true,
            suffix = {
                if (currentAmount > 0) {
                    Text(
                        text = "${DecimalFormat("#.#").format(percentage)}%",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        )

        if (showDelete) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Slet kategori",
                    tint = Color.Gray
                )
            }
        }
    }
}
//endregion

//region BUDGETPAGE UI
/**
 * The UI for the full budget page with graph
 */
@Composable
fun BudgetPageUI(
    modifier: Modifier = Modifier,
    currentBudget: Float,
    totalSpent: Float,
    receipts: List<ReceiptWithProducts>,
    expenses: List<ExtraExpense>,
    selectedMonth: Month = Month.from(LocalDate.now()),
    selectedYear: Year = Year.from(LocalDate.now()),
    onAddExpense: (String, Float) -> Unit,
    onMonthSelected: (Month, Year) -> Unit,
    onBudgetChanged: (Float) -> Unit,
    onDeleteReceipt: (ReceiptWithProducts) -> Unit,
    onDeleteExpense: (ExtraExpense) -> Unit
) {
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showViewReceiptsDialog by remember { mutableStateOf(false) }

    val remaining = currentBudget - totalSpent
    val spentPercentage = if (currentBudget > 0) (totalSpent / currentBudget).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .padding(12.dp)
            .fillMaxSize()
    ) {
        //MONTH DROPDOWN AND ADD BUTTON
        BudgetHeader(
            selectedMonth = "${selectedMonth.toDanishString()} ${selectedYear.value}",
            onMonthClick = { showMonthPicker = true },
            onAddClick = { showAddExpenseDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        //BUDGET CIRCLE
        BudgetCircle(
            currentBudget = currentBudget,
            totalSpent = totalSpent,
            remaining = remaining,
            spentPercentage = spentPercentage,
            onPriceChanged = { newPrice ->
                onBudgetChanged(newPrice)
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        //VIEW RECEIPTS
        ViewReceiptsButton(
            onClick = { showViewReceiptsDialog = true }
        )

        Spacer(modifier = Modifier.height(32.dp))

        //TIPS
        BudgetTipsSection(
            tips = getCurrentTips(spentPercentage)
        )
    }

    //region DIALOGS
    //Add expense dialog
    if (showAddExpenseDialog) {
        AddExpenseDialog(
            onDismiss = { showAddExpenseDialog = false },
            onConfirm = { name, price ->
                onAddExpense(name, price)
                showAddExpenseDialog = false
            }
        )
    }

    //Month picker dialog
    if (showMonthPicker) {
        DatePickerDialog(
            currentMonth = selectedMonth,
            currentYear = selectedYear,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { month, year ->
                onMonthSelected(month, year)
                showMonthPicker = false
            }
        )
    }

    //View receipts dialog
    if (showViewReceiptsDialog) {
        ViewExpensesDialog(
            onDismiss = { showViewReceiptsDialog = false },
            receipts = receipts,
            expenses = expenses,
            selectedMonth = selectedMonth.toDanishString(),
            onDeleteReceipt = onDeleteReceipt,
            onDeleteExpense = onDeleteExpense
        )
    }
    //endregion
}

@Composable
private fun BudgetHeader(selectedMonth: String, onMonthClick: () -> Unit, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //region Month selector dropdown
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onMonthClick() },
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedMonth,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Icon(
                    imageVector = ImageVector.vectorResource(R.drawable.dropdown_icon),
                    contentDescription = "Vælg måned",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        //endregion

        Spacer(modifier = Modifier.width(12.dp))

        //region Add button
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.add_icon),
                contentDescription = "Tilføj udgift"
            )
        }
        //endregion
    }
}

@Composable
fun BudgetCircle(modifier: Modifier = Modifier, currentBudget: Float, totalSpent: Float, remaining: Float, spentPercentage: Float, onPriceChanged: ((Float) -> Unit)? = null) {
    val animatedProgress by animateFloatAsState(
        targetValue = spentPercentage,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "BudgetProgress"
    )

    val backgroundCircleColor = MaterialTheme.colorScheme.surfaceContainer
    var changePrice by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(250.dp)
            .then(
                if (onPriceChanged != null) {
                    Modifier.clickable { changePrice = true }
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(200.dp)
        ) {
            val strokeWidth = 20.dp.toPx()
            val radius = size.width / 2 - strokeWidth / 2

            //Background circle
            drawCircle(
                color = backgroundCircleColor,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            //Progress arc
            if (animatedProgress > 0) {
                drawArc(
                    color = Color.Red,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        //Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tilbage:",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${formatInputToDanishCurrency(remaining.toInt().toString())}kr",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (remaining < 0) Color.Red else MaterialTheme.colorScheme.primaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Brugt:",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${formatInputToDanishCurrency(totalSpent.toInt().toString())}kr",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }

    if (onPriceChanged != null && changePrice) {
        ChangePriceDialog(
            originalBudget = currentBudget,
            onDismiss = { changePrice = false },
            onConfirm = { newPrice ->
                onPriceChanged(newPrice)
                changePrice = false
            }
        )
    }
}

@Composable
private fun BudgetTipsSection(tips: List<String>) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "Tips & Indsigter",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "💡",
                fontSize = 16.sp
            )
        }

        tips.forEach { tip ->
            Text(
                text = "- $tip",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp),
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun AddExpenseDialog(onDismiss: () -> Unit, onConfirm: (String, Float) -> Unit) {
    var name by remember { mutableStateOf("") }
    var price by remember { mutableStateOf(TextFieldValue("")) }
    val isValid = name.trim().isNotEmpty() && price.text.trim().isNotEmpty()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Ekstra udgift",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Navn...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { input ->
                        val validInput = filterInputToValidNumberInput(input.text)

                        if (!validInput.isEmpty()) {
                            val formatted = formatInputToDanishCurrency(validInput)
                            price = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        }
                    },
                    label = { Text("Pris...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Annuller",
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val priceFloat = danishCurrencyToFloat(price.text)
                            if (name.isNotBlank() && priceFloat > 0) {
                                onConfirm(name, priceFloat)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        enabled = isValid
                    ) {
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

@Composable
private fun DatePickerDialog(currentMonth: Month?, currentYear: Year?, onDismiss: () -> Unit, onMonthSelected: (Month, Year) -> Unit) {
    val years = listOf(Year.of(2025))
    val year2Month = years.flatMap { year ->
        Month.entries.map { month ->
            month to year
        }
    }

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
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Vælg måned",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                year2Month.forEach { (month, year) ->
                    val isSelected = month == currentMonth && year == currentYear

                    Text(
                        text = "${month.toDanishString()} ${year.value}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMonthSelected(month, year) }
                            .padding(vertical = 12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Luk",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewExpensesDialog(
    onDismiss: () -> Unit,
    receipts: List<ReceiptWithProducts>,
    expenses: List<ExtraExpense>,
    selectedMonth: String,
    onDeleteReceipt: (ReceiptWithProducts) -> Unit,
    onDeleteExpense: (ExtraExpense) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Udgifter for $selectedMonth",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))


                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(receipts) { receipt ->
                        ReceiptCard(
                            receipt = receipt,
                            onDeleteReceipt = onDeleteReceipt
                        )
                    }
                    items(expenses) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onDeleteExpense = onDeleteExpense
                        )
                    }
                }

                //Bottom bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Luk",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChangePriceDialog(originalBudget: Float, onDismiss: () -> Unit, onConfirm: (Float) -> Unit) {
    var newBudget by remember { mutableStateOf(TextFieldValue("")) }

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
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Skift budgetmål",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = newBudget,
                    onValueChange = { input ->
                        val validInput = filterInputToValidNumberInput(input.text)

                        if (!validInput.isEmpty()) {
                            val formatted = formatInputToDanishCurrency(validInput)

                            //Makes sure curser is always at the end of the number
                            newBudget = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        }
                    },
                    label = { Text(
                        text = "Nuværende: ${formatFloatToDanishCurrency(originalBudget)}",
                        color = Color.Gray,
                        fontStyle = FontStyle.Italic
                    )},
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Annuller",
                            color = Color.Gray
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(danishCurrencyToFloat(newBudget.text)) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "Tilføj",
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReceiptDialog(onDismiss: () -> Unit, receipt: ReceiptWithProducts, onDeleteReceipt: (ReceiptWithProducts) -> Unit) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${DateTimeFormatter.ofPattern("dd/MM").format(receipt.receipt.date)}:",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "${formatFloatToDanishCurrency(receipt.receipt.total)}kr",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }

                LazyColumn {
                    items(receipt.products) { product ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                modifier = Modifier
                                    .weight(1f, fill = false),
                                text = product.name,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1
                            )
                            Text(
                                text = formatFloatToDanishCurrency(product.price),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { showDeleteDialog = true }) {
                        Text(
                            text = "Slet",
                            color = Color.Gray
                        )
                    }

                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Luk",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            title = "Slet kvittering?",
            body = "Er du sikker på du vil slette kvitteringen? Dette kan ikke fortrydes",
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDeleteReceipt(receipt)
                showDeleteDialog = false
            },
        )
    }
}

@Composable
private fun ReceiptCard(modifier: Modifier = Modifier, receipt: ReceiptWithProducts, onDeleteReceipt: (ReceiptWithProducts) -> Unit) {
    var openReceipt by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = { openReceipt = true })
            .padding(horizontal = 8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${receipt.receipt.store}: ${formatFloatToDanishCurrency(receipt.receipt.total)}kr",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = DateTimeFormatter.ofPattern("dd/MM").format(receipt.receipt.date),
                style = MaterialTheme.typography.bodyLarge,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    if (openReceipt) {
        ReceiptDialog(
            onDismiss = { openReceipt = false },
            onDeleteReceipt =  { receipt ->
                onDeleteReceipt(receipt)
                openReceipt = false
            },
            receipt = receipt
        )
    }
}

@Composable
private fun ExpenseCard(modifier: Modifier = Modifier, expense: ExtraExpense, onDeleteExpense: (ExtraExpense) -> Unit) {
    var showDeleteExpenseDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .clickable { showDeleteExpenseDialog = true },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${expense.name.trim()}: ${formatFloatToDanishCurrency(expense.price)}kr",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = DateTimeFormatter.ofPattern("dd/MM").format(expense.date),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }


    //region DIALOGS
    if (showDeleteExpenseDialog) {
        DeleteConfirmationDialog(
            title = "Slet udgift?",
            body = "Er du sikker på du vil slette: \n ${expense.name}",
            onDismiss = { showDeleteExpenseDialog = false },
            onConfirm = {
                onDeleteExpense(expense)
                showDeleteExpenseDialog = false
            }
        )
    }
    //endregion
}

@Composable
private fun ViewReceiptsButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "Se udgifter for måneden",
            color = MaterialTheme.colorScheme.onPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
//endregion

//region HELPER FUNCTIONS
private fun getCurrentTips(spentPercentage: Float): List<String> {
    return when {
        spentPercentage > 1.0f -> BudgetTips.OVERSPENDING.tips
        spentPercentage < 0.7f -> BudgetTips.UNDER_BUDGET.tips
        else -> BudgetTips.GENERAL.tips
    }
}
//endregion
