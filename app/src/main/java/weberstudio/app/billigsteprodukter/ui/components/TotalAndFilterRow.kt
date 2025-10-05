package weberstudio.app.billigsteprodukter.ui.components

import android.R.attr.scaleY
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        //Total
        ReceiptTotalCard(
            modifier = Modifier.weight(1f),
            totalPrice = totalPrice
        )

        Spacer(modifier = Modifier.width(8.dp))

        //Filter
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .aspectRatio(1f)
                .clickable { filterMenuOnClick },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = ImageVector.vectorResource(R.drawable.sort_icon),
                contentDescription = "Filter",
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scaleX = -1f, scaleY = 1f) //Mirrors icon
            )
        }

    }
}