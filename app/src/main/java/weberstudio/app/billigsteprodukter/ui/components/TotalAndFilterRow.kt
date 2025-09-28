package weberstudio.app.billigsteprodukter.ui.components

import android.R.attr.scaleY
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import weberstudio.app.billigsteprodukter.R


/**
 * Shows the total price a of the receipt
 */
@Composable
fun TotalAndFilterRow(modifier: Modifier = Modifier, totalPrice: String, filterMenuOnClick: () -> Unit ) {
    Row(
        modifier = modifier
            .fillMaxSize(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Total
        ReceiptTotalCard(modifier = Modifier.weight(1f), totalPrice = totalPrice)

        //Filter
        IconButton(
            onClick = filterMenuOnClick,
            modifier = Modifier
                .size(56.dp)
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.sort_icon),
                contentDescription = "Filter",
                modifier = Modifier
                    .scale(scaleX = -1f, scaleY = 1f) //Mirrors icon
            )
        }
    }
}