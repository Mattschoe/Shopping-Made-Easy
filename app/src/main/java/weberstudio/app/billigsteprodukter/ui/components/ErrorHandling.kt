package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * A Large error message that pops a alert box and informs the user about the error
 * @param onDismissRequest what should be guaranteed to run (Often cleanup from VM). **REMEMBER** TO INCLUDE *navController.popBackStack()*, else the box wont disappear
 * @param errorTitle the title of the error message
 * @param errorMessage info about the error
 * @param onConfirmError what should be done when the user presses "Prøv igen"?
 * @param onDismissError what should be done when the user presses "Annuller"
 */
@Composable
fun ErrorMessageLarge(errorTitle: String, errorMessage: String, onDismissRequest: () -> Unit, onConfirmError: () -> Unit, onDismissError: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(errorTitle) },
        text = { Text(errorMessage)},
        confirmButton = {
            TextButton(onClick = onConfirmError) {
                Text("Prøv igen")
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissError()
                onDismissRequest()
            }) {
                Text("Annuller")
            }
        }
    )
}