package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import weberstudio.app.billigsteprodukter.data.AdsID

@Composable
fun MediumRectangleBannerAd(ad: AdsID, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.MEDIUM_RECTANGLE)
                    adUnitId = ad.ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun BannerAd(ad: AdsID, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = ad.ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

@Composable
fun LargeBannerAd(ad: AdsID, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
    ) {
        AndroidView(
            modifier = Modifier.fillMaxWidth(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.LARGE_BANNER)
                    adUnitId = ad.ID
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}