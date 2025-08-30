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
import weberstudio.app.billigsteprodukter.ui.navigation.PageNavigation

/**
 * The layout UI for the quick actions buttons
 */
@Composable
fun QuickActionsUI(modifier: Modifier, onClick: (PageNavigation) -> Unit) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        //Shopping List
        QuickActionsButton(
            text = "Opret indkøbsliste",
            iconRes = R.drawable.list,
            onClick = { onClick(PageNavigation.ShoppingList) },
            modifier = Modifier
                .weight(1f, fill = false)
        )

        //Budget
        QuickActionsButton(
            text = "Opret Budget",
            iconRes = R.drawable.list,
            onClick = { onClick(PageNavigation.Budget) },
            modifier = Modifier
                .weight(1f, fill = false))
    }
}

/**
 * Buttons for quick actions. Max 2 per row
 */
@Composable
fun QuickActionsButton(text: String, @DrawableRes iconRes: Int, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.secondary
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
                color = MaterialTheme.colorScheme.onSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}