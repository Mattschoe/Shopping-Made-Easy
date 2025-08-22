package weberstudio.app.billigsteprodukter.ui.pages.budget

/**
 * Budget tips enum
 */
enum class BudgetTips(val tips: List<String>) {
    GENERAL(
        listOf(
            "Du bruger flest penge i Netto, har du checket om du kan spare ved at købe ind i andre butikker?",
            "Fordel produkterne ind i kategorier for at bedre se hvor pengene bliver brugt!"
        )
    ),
    OVERSPENDING(
        listOf(
            "Du har overskredet dit budget! Overvej at justere dine udgifter.",
            "Prøv at lave en ugentlig madplan for at spare penge på mad."
        )
    ),
    UNDER_BUDGET(
        listOf(
            "Godt gået! Du holder dig under budgettet.",
            "Overvej at spare de ekstra penge op til næste måned."
        )
    )
}