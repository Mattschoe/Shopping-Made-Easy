package weberstudio.app.billigsteprodukter.ui.pages.home

import androidx.compose.runtime.getValue
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.data.ActivityType
import weberstudio.app.billigsteprodukter.data.AdsID
import weberstudio.app.billigsteprodukter.data.ExtraExpense
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import weberstudio.app.billigsteprodukter.data.RecentActivity
import weberstudio.app.billigsteprodukter.data.getIcon
import weberstudio.app.billigsteprodukter.logic.ActivityViewModel
import weberstudio.app.billigsteprodukter.logic.Formatter.toDanishString
import weberstudio.app.billigsteprodukter.ui.components.BannerAd
import weberstudio.app.billigsteprodukter.ui.components.PagerIndicator
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation
import weberstudio.app.billigsteprodukter.ui.pages.budget.BudgetCircle
import weberstudio.app.billigsteprodukter.ui.pages.budget.BudgetViewModel
import java.time.LocalDateTime
import java.time.Month

/**
 * The UI content of the *Main* Page
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainPageContent(
    modifier: Modifier = Modifier,
    navController: NavController,
    budgetViewModel: BudgetViewModel,
    mainPageViewModel: MainPageViewModel,
    activityViewModel: ActivityViewModel
) {
    val currentBudget by budgetViewModel.currentBudget.collectAsState()
    val currentReceipts by budgetViewModel.currentReceipts.collectAsState()
    val currentExpenses by budgetViewModel.currentExtraExpenses.collectAsState()

    val hasCompletedOnboarding by mainPageViewModel.hasCompletedOnboarding.collectAsState(initial = true)
    val hasVisitedReceiptPage by mainPageViewModel.hasVisitedReceiptPage.collectAsState(initial = true)
    val hasBeenWarnedAboutReceipt by mainPageViewModel.hasBeenWarnedAboutReceiptReadability.collectAsState(initial = true)
    var showParsersAvailableDialog by remember { mutableStateOf(false) }

    val recentActivities by activityViewModel.recentActivities.collectAsState(initial = emptyList())


    //Main page
    Column(
        modifier = modifier
            .padding(12.dp) //Standard padding from screen edge
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //region BUDGET CARD
        Text(
            text = "${currentBudget?.month?.toDanishString() ?: Month.from(LocalDateTime.now()).toDanishString()} Budget",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (currentBudget == null) {
            NoBudgetCard(
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(PageNavigation.Budget.route) }
            )
        } else {
            currentBudget?.let { currentBudget ->
                BudgetCard(
                    onClick = { navController.navigate(PageNavigation.Budget.route) },
                    currentBudget = currentBudget.budget,
                    currentReceipts = currentReceipts,
                    currentExpenses = currentExpenses
                )
            }
        }
        //endregion

        BannerAd(AdsID.MAINPAGE_BANNER)

        //region LATEST ACTIVITY
        if (currentBudget != null) {
            Column(
                modifier = Modifier
                    .weight(1f)
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
                    items(recentActivities) { activity ->
                        LatestActivityCard(
                            modifier = Modifier,
                            activity = activity,
                            onClick = {
                                when(activity.activityType) {
                                    ActivityType.RECEIPT_SCANNED -> navController.navigate(PageNavigation.createReceiptRoute(activity.receiptID!!))
                                    ActivityType.BUDGET_CREATED -> navController.navigate(PageNavigation.createBudgetRoute(activity.budgetMonth!!, activity.budgetYear!!))
                                    ActivityType.SHOPPING_LIST_CREATED -> navController.navigate(PageNavigation.createShoppingListDetailRoute(activity.shoppingListID!!))
                                }
                            }
                        )
                    }
                }
            }
        }
        //endregion
    }

    if (!hasCompletedOnboarding) {
        OnboardingDialog(
            onDismiss = {
                mainPageViewModel.setOnboardingCompleted(true)
                showParsersAvailableDialog = true
            },
            onConfirm = {
                mainPageViewModel.setOnboardingCompleted(true)
                showParsersAvailableDialog = true
            },
        )
    }

    if (showParsersAvailableDialog) {
        ParsersAvailableDialog(
            onDismiss = { showParsersAvailableDialog = false },
            onConfirm = { showParsersAvailableDialog = false },
        )
    }

    if (hasVisitedReceiptPage && !hasBeenWarnedAboutReceipt) {
        WarnAboutReceiptDialog(
            onDismiss = { mainPageViewModel.setHasBeenWarnedAboutReceipt(true) },
            onConfirm = { mainPageViewModel.setHasBeenWarnedAboutReceipt(true) },
        )
    }
}

@Composable
fun WarnAboutReceiptDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 6.dp,
            modifier = modifier
                .padding(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .widthIn(min = 280.dp, max = 380.dp)
            ) {
                //Titel
                Text(
                    text = "Hey!",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Body
                Text(
                    text = "Håber scanningen gik godt! Nogle gange kan der komme problemer med at scanneren læser teksten bag på tynde kvitteringer. Hvis du oplever at produkterne får mærkelige navne så prøv at strege teksten bag på kvitteringen ud med en tusch før scanning",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm
                    ) {
                        Text(
                            text = "Ok",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    initialSelection: Int = 0,
) {
    val title = "Velkommen til!"
    val animations = listOf<Pair<String, Int>>(
        Pair("Scan din kvittering", R.raw.onboardingpage1_animation),
        Pair("Opret indkøbslister, vælg produkter hvor det er billigst", R.raw.onboardingpage2_animation),
        Pair("Sæt et budget og få overblik", R.raw.onboardingpage3_animation),
    )
    val pagerState = rememberPagerState(
        initialPage = initialSelection,
        pageCount = { animations.size }
    )
    val isValid = pagerState.currentPage == animations.size - 1

    Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = modifier
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
                Spacer(Modifier.height(12.dp))

                //region DISPLAY
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                ) { page ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
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
                                text = animations[page].first,
                                style = MaterialTheme.typography.titleMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            //Animation
                            val composition by rememberLottieComposition(
                                LottieCompositionSpec.RawRes(animations[page].second)
                            )

                            val isLoaded = composition != null

                            val progress by animateLottieCompositionAsState(
                                composition = composition,
                                iterations = LottieConstants.IterateForever
                            )

                            if (isLoaded) {
                                LottieAnimation(
                                    composition = composition,
                                    progress = { progress },
                                    modifier = Modifier
                                        .fillMaxSize()
                                )
                            } else {
                                CircularProgressIndicator(Modifier.size(100.dp))
                            }
                        }
                    }
                }

                //endregion

                //region DOTS
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    PagerIndicator(
                        pageCount = animations.size,
                        currentPage = pagerState.currentPage,
                        currentPageOffsetFraction = pagerState.currentPageOffsetFraction,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                //endregion

                //region OK/ANNULER
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
                        onClick = onConfirm
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
fun ParsersAvailableDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 6.dp,
            modifier = modifier
                .padding(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer
        ) {
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .widthIn(min = 280.dp, max = 380.dp)
            ) {
                //Titel
                Text(
                    text = "Butikskvitteringer som kan scannes",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(12.dp))

                //Body
                Text(
                    text = "Vi arbejder hele tiden på at tilføjer nye kvitteringer, men det tager desværre tid. For nu kan kvitteringer fra disse butikker scannes:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                Text(text = "- Netto", fontWeight = FontWeight.SemiBold)
                Text(text = "- Coop365", fontWeight = FontWeight.SemiBold)
                Text(text = "- SuperBrugsen", fontWeight = FontWeight.SemiBold)
                Text(text = "- Lidl", fontWeight = FontWeight.SemiBold)
                Text(text = "- Rema1000", fontWeight = FontWeight.SemiBold)

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirm
                    ) {
                        Text(
                            text = "Ok",
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun NoBudgetCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BudgetCircle(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .aspectRatio(1f),
            currentBudget = 0f,
            totalSpent = 0f,
            remaining = 0f,
            spentPercentage = 0f,
        )

        Spacer(modifier = Modifier.height(16.dp))

        //Button
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
            currentBudget = currentBudget,
            totalSpent = totalSpent,
            remaining = remaining,
            spentPercentage = spentPercentage,
        )
    }
}

@Composable
fun LatestActivityCard(modifier: Modifier = Modifier, activity: RecentActivity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                painter = painterResource(activity.getIcon()),
                modifier = Modifier
                    .size(36.dp),
                contentDescription = activity.toString()
            )

            //Content
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = activity.displayInfo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = formatTimestamp(activity.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val difference = now - timestamp
    val dayInMillis = 24 * 60 * 60 * 1000

    return when {
        difference < dayInMillis -> {
            val hours = difference / (60 * 60 * 1000)
            if (hours < 1) {
                val minutes = difference / (60 * 1000)
                if (minutes < 1) "Nu" else "${minutes}m siden"
            } else {
                val dateFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale("da", "DK"))
                "I dag, ${dateFormat.format(java.util.Date(timestamp))}"
            }
        }
        difference < 2 * dayInMillis -> {
            val dateFormat = java.text.SimpleDateFormat("HH:mm", java.util.Locale("da", "DK"))
            "I går, ${dateFormat.format(java.util.Date(timestamp))}"
        }
        difference < 7 * dayInMillis -> {
            val days = (difference / dayInMillis).toInt()
            "${days} dage siden"
        }
        else -> {
            val dateFormat = java.text.SimpleDateFormat("dd/MM", java.util.Locale("da", "DK"))
            dateFormat.format(java.util.Date(timestamp))
        }
    }
}

