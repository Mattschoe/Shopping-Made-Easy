package weberstudio.app.billigsteprodukter.ui.pages.home

import android.app.Activity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.logic.CameraViewModel
import weberstudio.app.billigsteprodukter.ui.components.LatestActivityButton
import weberstudio.app.billigsteprodukter.ui.components.SaveImage
import weberstudio.app.billigsteprodukter.ui.components.SaveImageButton
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation
import weberstudio.app.billigsteprodukter.ui.pages.budget.BudgetViewModel
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.ExtraExpense
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import weberstudio.app.billigsteprodukter.ui.pages.budget.BudgetCircle
import weberstudio.app.billigsteprodukter.ui.theme.Typography

/**
 * The UI content of the *Main* Page
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainPageContent(modifier: Modifier = Modifier, navController: NavController, cameraViewModel: CameraViewModel, budgetViewModel: BudgetViewModel) {
    val budgetViewModel: BudgetViewModel = viewModel()
    val currentBudget by budgetViewModel.currentBudget.collectAsState()
    val currentReceipts by budgetViewModel.currentReceipts.collectAsState()
    val currentExpenses by budgetViewModel.currentExtraExpenses.collectAsState()

    //Main page
    Column(
        modifier = modifier
            .padding(12.dp) //Standard padding from screen edge
            .fillMaxSize()
            //.border(2.dp, Color.Magenta)
                ,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //region BUDGET CARD
        Text(
            text = "Budget",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (currentBudget == null) {
            Column(
                modifier = Modifier
                    .weight(1.5f)
                    .border(2.dp, Color.Magenta)
            ) {
                NoBudgetCard(
                    onClick = { navController.navigate(PageNavigation.Budget.route) }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .border(2.dp, Color.Magenta)
            ) {
                currentBudget?.let { currentBudget ->
                    BudgetCard(
                        onClick = { navController.navigate(PageNavigation.Budget.route) },
                        currentBudget = currentBudget.budget,
                        currentReceipts = currentReceipts,
                        currentExpenses = currentExpenses
                    )
                }
            }
        }
        //endregion

        Spacer(Modifier.height(4.dp))

        //region LATEST ACTIVITY
        Column(
            modifier = Modifier
                .weight(1f)
                //.border(2.dp, Color.Red)
        ) {
            Text(
                text = "Seneste Aktivitet",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            LazyColumn(
                modifier = Modifier
            ) {
                items(10) { _ ->
                    LatestActivityButton("Hejsa")
                }
            }
        }
        //endregion
    }
}

@Composable
fun NoBudgetCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BudgetCircle(
            modifier = Modifier.weight(1f),
            totalSpent = 0f,
            remaining = 0f,
            spentPercentage = 0f
        )

        Spacer(modifier = Modifier.height(16.dp))

        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.primary,
            shadowElevation = 4.dp,
            tonalElevation = 2.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Opret budget",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Lad os få oprettet et budget for måneden!",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun BudgetCard(modifier: Modifier = Modifier, onClick: () -> Unit, currentBudget: Float, currentReceipts: List<ReceiptWithProducts>, currentExpenses: List<ExtraExpense>) {
    val totalSpent = (currentReceipts.sumOf { it.receipt.total.toDouble() } + currentExpenses.sumOf { it.price.toDouble() }).toFloat()
    val remaining = currentBudget - totalSpent
    val spentPercentage = if (currentBudget > 0) (totalSpent / currentBudget).coerceIn(0f, 1f) else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        BudgetCircle(
            totalSpent = totalSpent,
            remaining = remaining,
            spentPercentage = spentPercentage
        )
    }
}


@Composable
fun LatestActivityCard(activity: Activity, onClick: () -> Unit) {

}