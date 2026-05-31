package weberstudio.app.billigsteprodukter.ui.pages.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.ExtraExpense
import weberstudio.app.billigsteprodukter.data.Receipt
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import weberstudio.app.billigsteprodukter.data.budget.BudgetRepository
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.Year

class BudgetViewModel(application: Application): AndroidViewModel(application) {
    private val app = application as ReceiptApp
    private val receiptRepo: ReceiptRepository = app.receiptRepository
    private val budgetRepo: BudgetRepository = app.budgetRepository

    //Den valgte periode er den eneste reaktive kilde; de tre states udledes herfra
    private val _selectedPeriod = MutableStateFlow(
        LocalDateTime.now().let { Month.from(it) to Year.from(it) }
    )
    val selectedPeriod: StateFlow<Pair<Month, Year>> = _selectedPeriod.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentBudget: StateFlow<Budget?> = _selectedPeriod
        .flatMapLatest { (month, year) -> budgetRepo.getBudget(month, year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentReceipts: StateFlow<List<ReceiptWithProducts>> = _selectedPeriod
        .flatMapLatest { (month, year) -> receiptRepo.getReceiptsForMonth(month, year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val currentExtraExpenses: StateFlow<List<ExtraExpense>> = _selectedPeriod
        .flatMapLatest { (month, year) -> budgetRepo.getExpenses(month, year) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Sets the period (month + year) the budget and expenses are shown for
     */
    fun selectPeriod(month: Month, year: Year) {
        _selectedPeriod.value = month to year
    }

    /**
     * Adds a new budget to the database
     */
    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepo.insertBudget(budget)
            app.activityLogger.logBudgetCreated(budget)
        }
    }

    fun addExtraSpendingToCurrentBudget(expenseName: String, price: Float) {
        viewModelScope.launch {
            budgetRepo.insertExtraExpense(ExtraExpense(
                name = expenseName,
                price = price,
                date = LocalDate.now(),
                month = Month.from(LocalDateTime.now()),
                year =  Year.from(LocalDateTime.now())
            ))
        }
    }

    fun updateBudget(newBudget: Budget) {
        viewModelScope.launch {
            budgetRepo.updateBudget(newBudget)
        }
    }

    fun deleteReceipt(receipt: Receipt) {
        viewModelScope.launch {
            receiptRepo.deleteReceipt(receipt)
        }
    }

    fun deleteExpense(expense: ExtraExpense) {
        viewModelScope.launch {
            budgetRepo.deleteExtraExpense(expense)
        }
    }
}