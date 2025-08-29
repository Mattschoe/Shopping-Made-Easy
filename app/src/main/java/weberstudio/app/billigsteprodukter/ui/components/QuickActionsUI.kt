package weberstudio.app.billigsteprodukter.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import weberstudio.app.billigsteprodukter.R

/**
 * The layout UI for the quick actions buttons
 */
@Composable
fun QuickActionsUI(modifier: Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CreateShoppingListUI(
            modifier = Modifier
                .weight(1f, fill = false)
        )

        TempUI(
            modifier = modifier
                .weight(1f,  fill = false)
        )
    }
}

/**
 * Buttons for quick actions. Max 2 per row
 */
@Composable
fun QuickActionsButton(text: String, @DrawableRes iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(iconRes),
                contentDescription = "Ikon af kvittering",
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(16.dp)) //Padding between icon and text

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Moves user to shopping list page so they can create their shopping list
 */
@Composable
fun CreateShoppingListUI(modifier: Modifier) {
    QuickActionsButton("Opret indkøbsliste", R.drawable.list, { println("Jeg vil gerne oprette min indkøbsliste!") }, modifier)
}

/**
 * Temporary name until i find a use for this
 */
@Composable
fun TempUI(modifier: Modifier) {
    QuickActionsButton("Temp UI", R.drawable.list, { println("Jeg vil gerne lave noget temp her!") }, modifier)
}