package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import weberstudio.app.billigsteprodukter.R


/**
 * @param productName the name of the product (Title)
 * @param productPrice the price of the product
 * @param onThreeDotMenuClick what should happen when the three dots are pressed
 */
@Composable
fun ProductRow(productName: String, productPrice: String, onThreeDotMenuClick: () -> Unit) {
    DefaultProductCard(
        modifier = Modifier
            .fillMaxSize()
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
                //Name
                Text(
                   text = productName,
                    style = MaterialTheme.typography.bodyLarge, //TODO: Change later to app font
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                //Price
                Text(
                    text = productPrice,
                    style = MaterialTheme.typography.bodyMedium, //TODO: Change later to app font
                    color = Color.Black
                )
            }
            //TODO: Det her dukker bare slet ikke op? Tror heller ikke der er funktionalitet for at ændre noget endnu
            IconButton(
                onClick = onThreeDotMenuClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.threedots_icon),
                    modifier = Modifier
                        .rotate(90f),
                    contentDescription = "Indstillinger for $productName",
                    tint = Color.Black
                )
            }
        }
    }
}


/**
 * The card showing the Total on the receipt
 */
@Composable
fun ReceiptTotalCard(modifier: Modifier = Modifier, totalPrice: String) {
    DefaultProductCard(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
        ) {
            Text(
                text = "Total:",
                style = MaterialTheme.typography.bodyMedium, //TODO: Change later to app font
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "${totalPrice}kr",
                style = MaterialTheme.typography.bodyMedium, //TODO: Change later to app font
                fontWeight = FontWeight.Normal,
                color = Color.Red,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * The standard UI Card for the ReceiptScanning oversigt
 */
@Composable
fun DefaultProductCard(modifier: Modifier = Modifier, content: @Composable (ColumnScope.() -> Unit)) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        content = content
    )
}

@Composable
fun AddProductToReceiptButton(modifier: Modifier = Modifier, addProductToReceipt: () -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = addProductToReceipt),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Green),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Tilføj produkter til kvitteringen",
                modifier = Modifier
                    .size(48.dp)
            )
        }
    }
}