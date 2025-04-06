package com.example.billigsteprodukter

import android.os.Bundle
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.billigsteprodukter.ui.theme.BilligsteProdukterTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { //Defines layout with the 'Composable' functions.
            BilligsteProdukterTheme {
                val questions = listOf("Mælk?", "Æg?", "Ost?", "Tis?")
                questionList(questions)
            }
        }
    }
}

@Composable
fun questionList(questions: List<String>) {
    Surface(color = MaterialTheme.colorScheme.primary) {
        Column {
            for (question in questions) {
                surveyAnswer(question);
            }
        }
    }
}

@Composable
fun surveyAnswer(question: String) {
    Text(question, padding(24.dp))
}
