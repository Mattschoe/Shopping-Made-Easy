package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.zIndex

@Composable
fun AddFAB(modifier: Modifier = Modifier, onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = Color(0xFF9AE66A),
        modifier = modifier
            .zIndex(1f) // make sure it's drawn above the list/cards
    ) {
        Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
    }
}