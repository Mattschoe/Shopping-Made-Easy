package com.example.billigsteprodukter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import com.example.billigsteprodukter.ui.theme.BilligsteProdukterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { //Defines layout with the 'Composable' functions.
            BilligsteProdukterTheme {
                Survey(true)
            }
        }
    }
}

@Composable
fun Survey(buttonClicked: Boolean) { //Enhver composable function skal have en modifier parameter
    Column {
        RadioButton(buttonClicked, onClick = {
            println("Test")
        })
    }
}
