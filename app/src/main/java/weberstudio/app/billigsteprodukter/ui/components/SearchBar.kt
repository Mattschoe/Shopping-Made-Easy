package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import weberstudio.app.billigsteprodukter.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(modifier: Modifier = Modifier, searchQuery: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        placeholder = { Text(text = "Søg... ", fontSize = 14.sp) },
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black),
        leadingIcon = { Icon(imageVector = ImageVector.vectorResource(R.drawable.searchbar_icon), contentDescription = null) },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedBorderColor = Color.Black,
            unfocusedBorderColor = Color.Black
        ),
        modifier = modifier
    )
}