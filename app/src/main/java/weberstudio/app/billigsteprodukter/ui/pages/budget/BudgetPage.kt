package weberstudio.app.billigsteprodukter.ui.pages.budget

import android.util.Log
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import java.text.DecimalFormat
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.Year
import java.util.Locale


@Composable
fun BudgetPage(modifier: Modifier = Modifier, viewModel: BudgetViewModel) {
    val currentBudget by viewModel.currentBudget.collectAsState()
    val currentReceipts by viewModel.currentReceipts.collectAsState()

    //Hvis useren ikke har givet budgetInput for de intro pagen
    if (currentBudget == null) {
        PreBudgetPageUI(
            modifier = modifier
        ) { newBudget ->
                viewModel.addBudget(newBudget)
        }
    }

    //Hvis useren har givet et budget så viser vi dem statistik pagen
    currentBudget?.let { currentBudget ->
        BudgetPageUI(
            modifier = modifier,
            currentBudget =  currentBudget.budget,
            totalSpent = currentReceipts.sumOf { it.receipt.total.toDouble() }.toFloat(),
            receipts = currentReceipts,
            selectedMonth = currentBudget.month,
            selectedYear = currentBudget.year,
            onAddExpense = { name, price -> viewModel.addExtraSpendingToCurrentBudget(name, price) },
            onMonthSelected = { month, year -> viewModel.loadBudget(month, year) }
        )
    }
}

//region UI
//region PreBudgetPageUI
val BudgetGreen = Color(0xFF4CAF50)
/**
 * The UI for the user to input a monthly budget so we can navigate them to [BudgetPageUI]
 */
@Composable
fun PreBudgetPageUI(modifier: Modifier = Modifier, newBudget: (Budget) -> Unit) {
    newBudget(Budget(
        month = Month.from(LocalDateTime.now()),
        year = Year.from(LocalDateTime.now()),
        budget = 2500f
    ))
    /* var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Budget",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "Hvad er budgettet på\nmad for denne måned?",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Black,
                    lineHeight = 22.sp
                )

                Button(
                    onClick = { showDialog = true },
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BudgetGreen // TODO: Change color here
                    )
                ) {
                    Text(
                        text = "Opret budget",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    }

    if (showDialog) {
        BudgetDialog(
            onDismiss = { showDialog = false }
        )
    }

     */
}

@Composable
fun BudgetDialog(onDismiss: () -> Unit) {
    var totalBudget by remember { mutableStateOf("") }
    var categories by remember { mutableStateOf(
        mutableListOf(
            BudgetCategory("gal", "Gal"),
            BudgetCategory("kod", "Kød"),
            BudgetCategory("frys", "Frys"),
            BudgetCategory("tor", "Tör")
        )
    ) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Text(
                    text = "Budget for Januar 2025",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Total budget field
                BudgetInputField(
                    label = "Ialt",
                    value = totalBudget,
                    onValueChange = { totalBudget = formatCurrency(it) },
                    totalBudget = parseAmount(totalBudget),
                    currentAmount = parseAmount(totalBudget),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Category fields
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
                                    categories[index] = category.copy(amount = formatCurrency(newValue))
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

                    // Add new category button
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
                                containerColor = Color.Transparent
                            ),
                            border = ButtonDefaults.outlinedButtonBorder().copy(
                                width = 1.dp
                            )
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Tilføj kategori",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tilføj kategori")
                        }
                    }
                }

                // Done button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(BudgetGreen) // TODO: Change color here
                    ) {
                        Icon(
                            Icons.Default.Check,
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
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    totalBudget: Double,
    currentAmount: Double,
    showDelete: Boolean = false,
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
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
                focusedBorderColor = BudgetGreen, // TODO: Change color here
                focusedLabelColor = BudgetGreen,  // TODO: Change color here
                unfocusedBorderColor = BudgetGreen.copy(alpha = 0.5f) // TODO: Change color here
            ),
            singleLine = true,
            suffix = {
                if (currentAmount > 0) {
                    Text(
                        text = "${DecimalFormat("#.#").format(percentage)}%",
                        fontSize = 12.sp,
                        color = Color.Gray
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

//region HELPER FUNCTIONS
fun formatCurrency(input: String): String {
    // Remove all non-digits
    val digitsOnly = input.filter { it.isDigit() }

    if (digitsOnly.isEmpty()) return ""

    // Convert to double and format
    val amount = digitsOnly.toDoubleOrNull() ?: 0.0
    val formatted = NumberFormat.getNumberInstance(Locale.forLanguageTag("da-DK")).format(amount)

    return formatted
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
    selectedMonth: Month = Month.from(LocalDate.now()),
    selectedYear: Year = Year.from(LocalDate.now()),
    onAddExpense: (String, Float) -> Unit,
    onMonthSelected: (Month, Year) -> Unit
) {
    var showAddExpenseDialog by remember { mutableStateOf(false) }
    var showMonthPicker by remember { mutableStateOf(false) }
    var showViewReceiptsDialog by remember { mutableStateOf(false) }

    val remaining = currentBudget - totalSpent
    val spentPercentage = if (currentBudget > 0) (totalSpent / currentBudget).coerceIn(0f, 1f) else 0f

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White) // TODO: Change to your app's background color
            .padding(16.dp)
    ) {
        // Header with dropdown and add button
        BudgetHeader(
            selectedMonth = "${selectedMonth.toDanishString()} ${selectedYear.value}",
            onMonthClick = { showMonthPicker = true },
            onAddClick = { showAddExpenseDialog = true }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Budget circle with animation
        BudgetCircle(
            currentBudget = currentBudget,
            totalSpent = totalSpent,
            remaining = remaining,
            spentPercentage = spentPercentage
        )

        Spacer(modifier = Modifier.height(24.dp))

        // View receipts button
        ViewReceiptsButton(
            onClick = { showViewReceiptsDialog = true }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Tips section
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
        ViewReceiptsDialog(
            onDismiss = { showViewReceiptsDialog = false },
            receipts = receipts,
            selectedMonth = selectedMonth.toDanishString()
        )
    }
    //endregion

}

@Composable
private fun BudgetHeader(selectedMonth: String, onMonthClick: () -> Unit, onAddClick: () -> Unit) {
    Text(
        text = "Budget",
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = Color.Black, // TODO: Change to your app's primary text color
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Month selector dropdown
        Card(
            modifier = Modifier
                .weight(1f)
                .clickable { onMonthClick() },
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5) // TODO: Change to your app's card background
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
                    color = Color.Black // TODO: Change to your app's text color
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Vælg måned",
                    tint = Color.Black // TODO: Change to your app's icon color
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Add button
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier.size(48.dp),
            containerColor = Color(0xFF4CAF50), // TODO: Change to your app's accent color
            contentColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tilføj udgift"
            )
        }
    }
}

@Composable
private fun BudgetCircle(currentBudget: Float, totalSpent: Float, remaining: Float, spentPercentage: Float) {
    // Animation for the progress ring
    val animatedProgress by animateFloatAsState(
        targetValue = spentPercentage,
        animationSpec = tween(durationMillis = 1000, easing = EaseOutCubic),
        label = "BudgetProgress"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier.size(200.dp)
        ) {
            val strokeWidth = 20.dp.toPx()
            val radius = size.width / 2 - strokeWidth / 2

            // Background circle
            drawCircle(
                color = Color(0xFFE0E0E0), // TODO: Change to your app's inactive color
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Progress arc
            if (animatedProgress > 0) {
                drawArc(
                    color = if (spentPercentage > 1f) Color.Red else Color(0xFFFF5722), // TODO: Change colors
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Tilbage:",
                fontSize = 14.sp,
                color = Color.Gray // TODO: Change to your app's secondary text color
            )
            Text(
                text = "${remaining.toInt()}kr",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (remaining < 0) Color.Red else Color.Black // TODO: Change colors
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Brugt:",
                fontSize = 12.sp,
                color = Color.Gray // TODO: Change to your app's secondary text color
            )
            Text(
                text = "${totalSpent.toInt()}kr",
                fontSize = 16.sp,
                color = Color(0xFFFF5722) // TODO: Change to your app's expense color
            )
        }
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
                color = Color.Black // TODO: Change to your app's primary text color
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
                color = Color.Black, // TODO: Change to your app's text color
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
                containerColor = Color.White // TODO: Change to your app's dialog background
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Ekstra udgift",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black // TODO: Change to your app's primary text color
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Navn...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50), // TODO: Change to your app's accent color
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Pris...") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50), // TODO: Change to your app's accent color
                        unfocusedBorderColor = Color.Gray
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Annuller",
                            color = Color.Gray // TODO: Change to your app's secondary text color
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
                            containerColor = Color(0xFF4CAF50) // TODO: Change to your app's accent color
                        )
                    ) {
                        Text("Tilføj")
                    }
                }
            }
        }
    }
}

@Composable
private fun DatePickerDialog(currentMonth: Month, currentYear: Year, onDismiss: () -> Unit, onMonthSelected: (Month, Year) -> Unit) {
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
                containerColor = Color.White // TODO: Change to your app's dialog background
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Vælg måned",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black // TODO: Change to your app's primary text color
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
                        color = if (isSelected) Color(0xFF4CAF50) else Color.Black, // TODO: Change colors
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
                            color = Color.Gray // TODO: Change to your app's secondary text color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewReceiptsDialog(onDismiss: () -> Unit, receipts: List<ReceiptWithProducts>, selectedMonth: String) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White // TODO: Change to your app's dialog background
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Udgifter for $selectedMonth",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black // TODO: Change to your app's primary text color
                )

                Spacer(modifier = Modifier.height(16.dp))

                // TODO: Customize this dialog content yourself
                Text(
                    text = "Crazy? I was crazy once. They locked me in a room. A rubber room. A rubber room with rats. And rats make me crazy. Crazy? I was crazy once. They locked me in a room. A rubber room. A rubber room with rats. And rats make me crazy",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Luk",
                            color = Color.Gray // TODO: Change to your app's secondary text color
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
            containerColor = Color(0xFF4CAF50) // TODO: Change to your app's accent color
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "Se udgifter for måneden",
            color = Color.White,
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
//endregion