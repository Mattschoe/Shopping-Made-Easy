package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(modifier: Modifier = Modifier, searchQuery: String, onQueryChange: (String) -> Unit) {
    //TODO Material3 er idiot og ikke lade mig ændre border size, når UI bliver genvisiteret skal det her nok ændres til et andet type textfield så border size kan justeres
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onQueryChange,
        placeholder = { Text(text = "Søg... ", fontSize = 14.sp) },
        singleLine = true,
        textStyle = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black),
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) }, //TODO: Fjern Placeholder for eget ikon
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