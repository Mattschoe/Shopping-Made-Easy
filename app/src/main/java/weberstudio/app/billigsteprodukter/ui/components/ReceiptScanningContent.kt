package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import weberstudio.app.billigsteprodukter.R
import weberstudio.app.billigsteprodukter.ui.theme.ThemeLightGreen
import weberstudio.app.billigsteprodukter.ui.theme.ThemeLightGrey
import weberstudio.app.billigsteprodukter.ui.theme.ThemeTEMP

/**
 * @param productName the name of the product (Title)
 * @param productPrice the price of the product
 * @param onThreeDotMenuClick what should happen when the three dots are pressed
 */
@Composable
fun ProductRow(productName: String, productPrice: String, onThreeDotMenuClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = ThemeTEMP),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                   text = productName,
                    style = MaterialTheme.typography.bodyLarge, //ToDo: Change later to app font
                    color = ThemeLightGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = productPrice,
                    style = MaterialTheme.typography.bodyMedium, //ToDo: Change later to app font
                    color = ThemeLightGrey
                )
            }
            IconButton(
                onClick = onThreeDotMenuClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.threedots_icon),
                    modifier = Modifier
                        .rotate(90f),
                    contentDescription = "Indstillinger for $productName",
                    tint = ThemeTEMP
                )
            }
        }
    }
}