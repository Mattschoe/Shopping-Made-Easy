package weberstudio.app.billigsteprodukter.MVVM

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import weberstudio.app.billigsteprodukter.ui.theme.BilligsteProdukterTheme


class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<ViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BilligsteProdukterTheme {
                println("HUH")
                viewModel.addProduct("Fakta", "Kikærter", 5f)
                viewModel.addProduct("Lidl", "Æbler", 2.5f)
                viewModel.addProduct("Netto", "Vodka", 70f)
                println("Tissemand")
            }
        }
    }
}