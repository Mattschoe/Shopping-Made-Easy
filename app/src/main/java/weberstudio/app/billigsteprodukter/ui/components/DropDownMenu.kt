package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import weberstudio.app.billigsteprodukter.logic.Store

@Composable
fun StoreScopeDropDownMenu(
    modifier: Modifier = Modifier,
    currentStore: Store,
    allStoresEnabled: Boolean,
    onAllStoresToggle: (Boolean) -> Unit,
) {
    var isDropDownExpanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE5FFEB)) //Temp
                .clickable { isDropDownExpanded = true }
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = if (!allStoresEnabled) currentStore.name else "Alle butikker",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(4.dp))
            Icon(
               imageVector = Icons.Default.ArrowDropDown, //TEMP
               contentDescription = "Skift søgning imellem ${currentStore.name} og alle butikker"
            )
        }
        DropdownMenu(
            expanded = isDropDownExpanded,
            onDismissRequest = { isDropDownExpanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            //Butiksnavn
            DropdownMenuItem(
                onClick = {
                    onAllStoresToggle(false)
                    isDropDownExpanded = false
                },
                text = { Text(text = currentStore.name, fontSize = 14.sp) }
            )

            //Alle butikker
            DropdownMenuItem(
                onClick = {
                    onAllStoresToggle(true)
                    isDropDownExpanded = false
                },
                text = { Text(text = "Alle butikker", fontSize = 14.sp) }
            )
        }
    }
}