package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MapUI(modifier: Modifier) {
    Box(
        modifier = modifier
    ) {
        Text(
            text = "Her skal der være et kort! :)",
            modifier = Modifier.align(Alignment.Center)
        )
    }
}