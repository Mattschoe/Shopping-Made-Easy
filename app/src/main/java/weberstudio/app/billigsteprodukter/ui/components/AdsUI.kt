package weberstudio.app.billigsteprodukter.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import weberstudio.app.billigsteprodukter.data.AdsID

/**
 * Delt banner-implementering. Reserverer pladsen ud fra [adSize] med det samme (ingen layout-shift),
 * afslører først reklamen når den er loadet, og binder [AdView]'ets livscyklus til compositionen
 * (resume/pause/destroy) for at undgå lækager og spildte off-screen impressions.
 */
@Composable
private fun BannerAdView(adSize: AdSize, ad: AdsID, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var loaded by remember { mutableStateOf(false) }

    val adView = remember(ad, adSize) {
        AdView(context).apply {
            setAdSize(adSize)
            adUnitId = ad.adUnitId
            adListener = object : AdListener() {
                override fun onAdLoaded() { loaded = true }
            }
            loadAd(AdRequest.Builder().build())
        }
    }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    //Centrerer reklamen i den fulde bredde, så den faste annoncestørrelse ikke lægger sig til venstre
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(adSize.width.dp)
                .height(adSize.height.dp)
                .alpha(if (loaded) 1f else 0f)
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { adView }
            )
        }
    }
}

@Composable
fun MediumRectangleBannerAd(ad: AdsID, modifier: Modifier = Modifier) =
    BannerAdView(AdSize.MEDIUM_RECTANGLE, ad, modifier)

@Composable
fun BannerAd(ad: AdsID, modifier: Modifier = Modifier) =
    BannerAdView(AdSize.BANNER, ad, modifier)

@Composable
fun LargeBannerAd(ad: AdsID, modifier: Modifier = Modifier) =
    BannerAdView(AdSize.LARGE_BANNER, ad, modifier)
