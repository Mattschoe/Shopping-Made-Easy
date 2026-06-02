package weberstudio.app.billigsteprodukter.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import weberstudio.app.billigsteprodukter.R
import androidx.compose.material3.Typography


val domineFamily = FontFamily(
    Font(R.font.domine_bold, FontWeight.Bold),
    Font(R.font.domine_regular, FontWeight.Normal),
    Font(R.font.domine_medium, FontWeight.Medium),
    Font(R.font.domine_semibold, FontWeight.SemiBold)
)
val ultraFont = FontFamily(Font(R.font.ultra_regular, FontWeight.Medium))
val baseline = Typography()
val smeTypography = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = ultraFont, fontWeight = FontWeight.Medium),
    displayMedium = baseline.displayMedium.copy(fontFamily = ultraFont, fontWeight = FontWeight.Medium),
    displaySmall = baseline.displaySmall.copy(fontFamily = ultraFont, fontWeight = FontWeight.Medium),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = ultraFont, fontWeight = FontWeight.Medium),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = ultraFont, fontWeight = FontWeight.Medium),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = ultraFont, fontWeight = FontWeight.Medium),
    titleLarge = baseline.titleLarge.copy(fontFamily = ultraFont, fontWeight = FontWeight.Medium),
    titleMedium = baseline.titleMedium.copy(fontFamily = ultraFont, fontWeight = FontWeight.Medium),
    titleSmall = baseline.titleSmall.copy(fontFamily = ultraFont, fontWeight = FontWeight.Medium),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = domineFamily, fontWeight = FontWeight.Medium),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = domineFamily, fontWeight = FontWeight.Medium),
    bodySmall = baseline.bodySmall.copy(fontFamily = domineFamily, fontWeight = FontWeight.Medium),
    labelLarge = baseline.labelLarge.copy(fontFamily = domineFamily, fontWeight = FontWeight.Medium),
    labelMedium = baseline.labelMedium.copy(fontFamily = domineFamily, fontWeight = FontWeight.Medium),
    labelSmall = baseline.labelSmall.copy(fontFamily = domineFamily, fontWeight = FontWeight.Medium)
)
