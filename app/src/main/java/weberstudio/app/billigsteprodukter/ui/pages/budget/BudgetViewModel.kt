package weberstudio.app.billigsteprodukter.ui.pages.budget

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import weberstudio.app.billigsteprodukter.ReceiptApp
import weberstudio.app.billigsteprodukter.data.Budget
import weberstudio.app.billigsteprodukter.data.ReceiptWithProducts
import weberstudio.app.billigsteprodukter.data.budget.BudgetRepository
import weberstudio.app.billigsteprodukter.data.receipt.ReceiptRepository
import java.time.LocalDate
import java.time.Month
import java.time.Year

class BudgetViewModel(application: Application): AndroidViewModel(application) {
    private val receiptRepo: ReceiptRepository = (application as ReceiptApp).receiptRepository
    private val budgetRepo: BudgetRepository = (application as ReceiptApp).budgetRepository

    private val _currentBudget = MutableStateFlow<Budget?>(null)
    private val _currentReceipts = MutableStateFlow<List<ReceiptWithProducts>>(emptyList())

    val currentBudget = _currentBudget.asStateFlow()
    val currentReceipts = _currentReceipts.asStateFlow()

    init {
        val now = LocalDate.now()
        loadBudget(Month.from(now), Year.from(now))
    }

    /**
     * Loads the budget and receipts given from the month and year
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
    }

    /**
     * Adds a new budget to the database
     */
    fun addBudget(budget: Budget) {
        viewModelScope.launch {
            budgetRepo.insertBudget(budget)
        }
    }

    fun addExtraSpendingToCurrentBudget(expenseName: String, price: Float) {

    }
}