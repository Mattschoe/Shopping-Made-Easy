package weberstudio.app.billigsteprodukter.data

import weberstudio.app.billigsteprodukter.BuildConfig


enum class AdsID(val ID: String) {
    BUDGETPAGE_BANNER("ca-app-pub-9304530596720570/4387801752"),
    SHOPPINGLIST_BANNER("ca-app-pub-9304530596720570/7260479626"),
    DATABASE_BANNER("ca-app-pub-9304530596720570/1172765182"),
    MAINPAGE_BANNER("ca-app-pub-9304530596720570/4505505534"),
    SETTINGS_BANNER("ca-app-pub-9304530596720570/4163736515");

    /** Test unit-ID i debug-builds for at undgå invalid-traffic på den rigtige konto. */
    val adUnitId: String
        get() = if (BuildConfig.DEBUG) TEST_BANNER_ID else ID

    companion object {
        // Googles officielle test-banner unit (dækker alle banner-størrelser inkl. MREC)
        private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    }
}
