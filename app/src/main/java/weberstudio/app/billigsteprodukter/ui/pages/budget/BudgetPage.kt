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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import weberstudio.app.billigsteprodukter.data.Receipt
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun BudgetPage(modifier: Modifier = Modifier, viewModel: BudgetViewModel) {
    /*BudgetUI(
        modifier = modifier,
        currentBudget =  viewModel.currentBudget,
        totalSpent = viewModel.totalSpent,
        receipts = ,
        onAddExpense = { arg1, arg2 ->  },
        onMonthSelected = { month -> viewModel.changeMonth(month)}
    ) */
}

//region UI
@Composable
fun BudgetUI(
    modifier: Modifier = Modifier,
    currentBudget: Float,
    totalSpent: Float,
    receipts: List<Receipt>,
    selectedMonth: String = getCurrentMonth(),
    onAddExpense: (String, Float) -> Unit,
    onMonthSelected: (String) -> Unit
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
            selectedMonth = selectedMonth,
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
        MonthPickerDialog(
            currentMonth = selectedMonth,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { month ->
                onMonthSelected(month)
                showMonthPicker = false
            }
        )
    }

    //View receipts dialog
    if (showViewReceiptsDialog) {
        ViewReceiptsDialog(
            onDismiss = { showViewReceiptsDialog = false },
            receipts = receipts,
            selectedMonth = selectedMonth
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
private fun MonthPickerDialog(currentMonth: String, onDismiss: () -> Unit, onMonthSelected: (String) -> Unit) {
    val months = listOf(
        "Januar 2025", "Februar 2025", "Marts 2025", "April 2025",
        "Maj 2025", "Juni 2025", "Juli 2025", "August 2025",
        "September 2025", "Oktober 2025", "November 2025", "December 2025"
    )

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

                months.forEach { month ->
                    Text(
                        text = month,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMonthSelected(month) }
                            .padding(vertical = 12.dp),
                        color = if (month == currentMonth) Color(0xFF4CAF50) else Color.Black, // TODO: Change colors
                        fontWeight = if (month == currentMonth) FontWeight.Bold else FontWeight.Normal
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
private fun ViewReceiptsDialog(onDismiss: () -> Unit, receipts: List<Receipt>, selectedMonth: String) {
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
private fun getCurrentMonth(): String {
    val sdf = SimpleDateFormat("MMMM yyyy", Locale("da", "DK"))
    return sdf.format(Date()).replaceFirstChar { it.uppercase() }
}

private fun getCurrentTips(spentPercentage: Float): List<String> {
    return when {
        spentPercentage > 1.0f -> BudgetTips.OVERSPENDING.tips
        spentPercentage < 0.7f -> BudgetTips.UNDER_BUDGET.tips
        else -> BudgetTips.GENERAL.tips
    }
}
//endregion