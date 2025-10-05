package weberstudio.app.billigsteprodukter.ui.pages.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val _currentBudget = MutableStateFlow<Budget?>(null)
    private val _currentReceipts = MutableStateFlow<List<ReceiptWithProducts>>(emptyList())
    private val _currentExtraExpenses = MutableStateFlow<List<ExtraExpense>>(emptyList())

    val currentBudget = _currentBudget.asStateFlow()
    val currentReceipts = _currentReceipts.asStateFlow()
    val currentExtraExpenses = _currentExtraExpenses

    init {
        val now = LocalDateTime.now()
        loadBudget(Month.from(now), Year.from(now))
    }

    /**
     * Loads the budget and expenses given from the month and year
     */
    fun loadBudget(month: Month, year: Year) {
        //Collects budget
        viewModelScope.launch {
            budgetRepo.getBudget(month, year).collect { budget ->
                _currentBudget.value = budget
            }
        }

        //Collects Receipts
        viewModelScope.launch {
            receiptRepo.getReceiptsForMonth(month, year).collect { receipts ->
                _currentReceipts.value = receipts
            }
        }

        //Collects extra expenses
        viewModelScope.launch {
            budgetRepo.getExpenses(month, year).collect { expenses ->
                _currentExtraExpenses.value = expenses
            }
        }
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