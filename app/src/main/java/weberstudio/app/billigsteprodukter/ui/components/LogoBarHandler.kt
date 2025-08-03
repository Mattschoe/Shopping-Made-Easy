package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * Shows the appropriate logo of the store.
 * @param storeName the name of the store
 */
@Composable
fun LogoBarHandler(modifier: Modifier = Modifier, storeName: String) {
    //Tries to lookup store name and display the image, else produces a alert for user if no store was found
    when (val result = lookupStore(storeName)) {
        is StoreLookupResult.Success -> {
            //If store was found
            Image(
                painter = painterResource(result.storeImage.logoRes),
                contentDescription = "logoet for $storeName",
                modifier = modifier
            )
        }
        is StoreLookupResult.Error -> {
            //If NO store was found
            println(result.errorMessage) //ToDo: Fix so this produces a alertdialog so the user can correctly choose the store
        }
    }
}

/**
 * Tries to lookup the store from the name.
 * @return **Success** if a store was found with the given name
 * @return **Error** if no store was found with the given name and returns a error string
 */
fun lookupStore(storeName: String): StoreLookupResult {
    return StoreImage.fromName(storeName)
        ?.let { StoreLookupResult.Success(it) }
        ?: StoreLookupResult.Error("No store found for name: $storeName")
}

sealed class StoreLookupResult {
    data class Success(val storeImage: StoreImage): StoreLookupResult()
    data class Error(val errorMessage: String): StoreLookupResult()
}