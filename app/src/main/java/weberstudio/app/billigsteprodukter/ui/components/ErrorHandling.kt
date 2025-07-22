package weberstudio.app.billigsteprodukter.ui.components

import android.app.AlertDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * A Large error message that pops a alert box and informs the user about the error
 * @param onDismissRequest what should be guaranteed to run (Often cleanup from VM)
 * @param errorTitle the title of the error message
 * @param errorMessage info about the error
 * @param onConfirmError
 */
@Composable
fun ErrorMessageLarge(onDismissRequest: () -> Unit, errorTitle: String, errorMessage: String, onConfirmError: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(errorTitle) },
        text = { Text(errorMessage)},
        confirmButton = {
            TextButton(onClick = onConfirmError) {
                Text("Ok")
            }
        }
    )
}