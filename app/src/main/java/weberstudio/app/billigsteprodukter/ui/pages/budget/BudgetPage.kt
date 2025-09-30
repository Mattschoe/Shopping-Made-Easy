package weberstudio.app.billigsteprodukter.ui.pages.budget

import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.Year
import java.time.format.DateTimeFormatter
import java.util.Locale


@Composable
fun BudgetPage(modifier: Modifier = Modifier, viewModel: BudgetViewModel) {
    val currentBudget by viewModel.currentBudget.collectAsState()
    val currentReceipts by viewModel.currentReceipts.collectAsState()
    val currentExpenses by viewModel.currentExtraExpenses.collectAsState()

    var selectedMonth by remember { mutableStateOf(Month.from(LocalDateTime.now())) }
    var selectedYear by remember { mutableStateOf(Year.from(LocalDateTime.now())) }

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
            }
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

    //region DIALOGS
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
    var totalBudget by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(
        mutableListOf(
            BudgetCategory("kol", "Køl"),
            BudgetCategory("frys", "Frys"),
            BudgetCategory("tor", "Tør")
        )
    ) }
    val currentYear by remember { mutableStateOf(Year.from(LocalDateTime.now())) }


    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                //Header
                Text(
                    text = "Budget for ${selectedMonth.toDanishString()} $currentYear",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                //Total budget field
                BudgetInputField(
                    label = "Ialt",
                    value = totalBudget,
                    onValueChange = { totalBudget = formatCurrencyToString(it) },
                    totalBudget = parseAmount(totalBudget),
                    currentAmount = parseAmount(totalBudget),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                //Category fields
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { category ->
                        BudgetInputField(
                            label = category.name,
                            value = category.amount,
                            onValueChange = { newValue ->
                                val index = categories.indexOfFirst { it.id == category.id }
                                if (index != -1) {
                                    categories[index] = category.copy(amount = formatCurrencyToString(newValue))
                                }
                            },
                            totalBudget = parseAmount(totalBudget),
                            currentAmount = parseAmount(category.amount),
                            showDelete = category.isDeletable,
                            onDelete = {
                                categories.removeAll { it.id == category.id }
                            }
                        )
                    }

                    //Add new category button
                    item {
                        OutlinedButton(
                            onClick = {
                                val newId = "custom_${System.currentTimeMillis()}"
                                categories.add(
                                    BudgetCategory(
                                        id = newId,
                                        name = "Ny kategori",
                                        isDeletable = true
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(28.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                width = 1.dp
                            )
                        ) {
                            Icon(
                                imageVector = ImageVector.vectorResource(R.drawable.check_icon),
                                contentDescription = "Tilføj kategori",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tilføj kategori")
                        }
                    }
                }

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
                                budget = formatCurrencyToFloat(totalBudget)
                            )
                            onClick(budget)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(
                            imageVector = ImageVector.vectorResource(R.drawable.check_icon),
                            contentDescription = "Færdig",
                            tint = Color.White,
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
    value: String,
    onValueChange: (String) -> Unit,
    totalBudget: Double,
    currentAmount: Double,
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

data class BudgetCategory(
    val id: String,
    val name: String,
    var amount: String = "",
    val isDeletable: Boolean = false
)
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
    onBudgetChanged: (Float) -> Unit
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
            selectedMonth = selectedMonth.toDanishString()
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
                text = "${formatCurrencyToString(remaining.toInt().toString())}kr",
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
                text = "${formatCurrencyToString(totalSpent.toInt().toString())}kr",
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
    var price by remember { mutableStateOf("") }

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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Pris...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
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
                            val priceFloat = price.toFloatOrNull()
                            if (name.isNotBlank() && priceFloat != null && priceFloat > 0) {
                                onConfirm(name, priceFloat)
                            }
                        },
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
private fun ViewExpensesDialog(onDismiss: () -> Unit, receipts: List<ReceiptWithProducts>, expenses: List<ExtraExpense>, selectedMonth: String) {
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
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(receipts) { receipt ->
                        ReceiptCard(receipt = receipt)
                    }
                    items(expenses) { expense ->
                        ExpenseCard(expense = expense)
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

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
                        val filtered = input.text.filter { it.isDigit() || it == ',' || it == '.' }

                        if (filtered.count { it == ',' } <= 1) {
                            val parts = filtered.split(",")
                            val validInput = if (parts.size > 1 && parts[1].length > 2) {
                                "${parts[0]},${parts[1].take(2)}"
                            } else {
                                filtered
                            }

                            val formatted = formatInputToDanishCurrency(validInput)

                            //Makes sure curser is always at the end of the number
                            newBudget = TextFieldValue(
                                text = formatted,
                                selection = TextRange(formatted.length)
                            )
                        }
                    },
                    label = { Text(
                        text = "Nuværende: ${formatCurrencyToString(originalBudget)}",
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
                        onClick = {
                            val formatted = newBudget.text.replace(".", "").replace(",", ".")
                            val priceFloat = formatted.toFloatOrNull()
                            if (priceFloat != null) {
                                onConfirm(priceFloat)
                            }
                        },
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
private fun ReceiptCard(modifier: Modifier = Modifier, receipt: ReceiptWithProducts) {
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
                text = "${receipt.receipt.store}: ${receipt.receipt.total}kr",
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
            receipt = receipt
        )
    }
}

@Composable
private fun ExpenseCard(modifier: Modifier = Modifier, expense: ExtraExpense) {
    Card(
        modifier = modifier
            .fillMaxWidth()
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
                text = "${expense.name.trim()}: ${formatCurrencyToString(expense.price)}kr",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = DateTimeFormatter.ofPattern("dd/MM").format(expense.date),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Composable
private fun ReceiptDialog(onDismiss: () -> Unit, receipt: ReceiptWithProducts) {
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
                        text = "${formatCurrencyToString(receipt.receipt.total)}kr",
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
                                text = formatCurrencyToString(product.price),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

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

//region HELPER FUNCTION
private fun getCurrentTips(spentPercentage: Float): List<String> {
    return when {
        spentPercentage > 1.0f -> BudgetTips.OVERSPENDING.tips
        spentPercentage < 0.7f -> BudgetTips.UNDER_BUDGET.tips
        else -> BudgetTips.GENERAL.tips
    }
}

/**
 * Extension function for translating Month objects
 */
fun Month.toDanishString(): String = when(this) {
    Month.JANUARY -> "Januar"
    Month.FEBRUARY -> "Februar"
    Month.MARCH -> "Marts"
    Month.APRIL -> "April"
    Month.MAY -> "Maj"
    Month.JUNE -> "Juni"
    Month.JULY -> "Juli"
    Month.AUGUST -> "August"
    Month.SEPTEMBER -> "September"
    Month.OCTOBER -> "Oktober"
    Month.NOVEMBER -> "November"
    Month.DECEMBER -> "December"
}

/**
 * Formats currencies into danish standard like so:
 * "1234" -> "1.234"
 * "12345" -> "12.345"
 * "1234567" -> "1.234.567"
 */
fun formatCurrencyToString(input: Number): String {
    val amount = input.toDouble()
    return NumberFormat.getNumberInstance(Locale.forLanguageTag("da-DK")).format(amount)
}

/**
 * Formats currencies into danish standard like so:
 * "1234" -> "1.234"
 * "12345" -> "12.345"
 * "1234567" -> "1.234.567"
 */
fun formatCurrencyToString(input: String): String {
    val isNegative = input.trimStart().startsWith("-")
    val digitsOnly = input.filter { it.isDigit() }

    if (digitsOnly.isEmpty()) return ""

    // Convert to double and format
    val amount = digitsOnly.toDoubleOrNull() ?: 0.0
    val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("da-DK")).format(amount)

    return if (isNegative) "-$formatted" else formatted
}

/**
 * Does the opposite of [formatCurrencyToString] so:
 * "1234" -> 1234f
 * "1234567" -> 1234567f
 * "1234,56" -> 1234.56f
 */
fun formatCurrencyToFloat(input: String): Float {
    val trimmed = input.trim()
    val isNegative = trimmed.startsWith("-")
    val processed = trimmed
        .removePrefix("-")
        .replace(".", "")
        .replace(",", ".")
        .toFloat()
    return if (isNegative) -processed else processed
}

/**
 * Formats raw numbers in strings from input fields to danish pretty looking strings that match danish standard
 */
fun formatInputToDanishCurrency(input: String): String {
    if (input.isEmpty()) return ""

    val parts = input.split(",")
    val intPart = parts[0].replace(".", "")
    val decimalPart = if (parts.size > 1) parts[1] else null

    val formattedInt = if (intPart.length > 3) {
        intPart.reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()
    } else {
        intPart
    }

    if (decimalPart != null) return "$formattedInt,$decimalPart"
    if (input.endsWith(",")) return "$formattedInt,"
    else return formattedInt
}

fun parseAmount(formattedAmount: String): Double {
    if (formattedAmount.isEmpty()) return 0.0

    // Remove currency symbols and parse
    val cleaned = formattedAmount
        .replace("[^0-9,.]".toRegex(), "")
        .replace(",", ".")

    return cleaned.toDoubleOrNull() ?: 0.0
}
//endregion
