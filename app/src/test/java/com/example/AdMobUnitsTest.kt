package com.example

import com.example.ads.AdMobUnits
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdMobUnitsTest {

    @Test
    fun testAllRequiredAdUnitsConfigured() {
        // 1. Taxigord v1
        assertEquals("ca-app-pub-1495262574338316/2803163788", AdMobUnits.INTERSTITIAL_TAXIGORD_V1)
        // 2. Taxobetapub
        assertEquals("ca-app-pub-1495262574338316/2903409205", AdMobUnits.INTERSTITIAL_TAXOBETAPUB)
        // 3. Banner vip
        assertEquals("ca-app-pub-1495262574338316/2452733430", AdMobUnits.BANNER_VIP)
        // 4. Rewarded / Prestamevip
        assertEquals("ca-app-pub-1495262574338316/8970040322", AdMobUnits.REWARDED_PRESTAMEVIP)
        // 5.
        assertEquals("ca-app-pub-1495262574338316/1182179137", AdMobUnits.INTERSTITIAL_SECURITY)
        // 6.
        assertEquals("ca-app-pub-1495262574338316/4962596335", AdMobUnits.INTERSTITIAL_OPTIMIZER)
        // 7.
        assertEquals("ca-app-pub-1495262574338316/2714752655", AdMobUnits.INTERSTITIAL_VAULT)
        // 8.
        assertEquals("ca-app-pub-1495262574338316/3503687777", AdMobUnits.INTERSTITIAL_AI)

        // All units start with publisher prefix
        val publisherPrefix = "ca-app-pub-1495262574338316/"
        assertTrue(AdMobUnits.INTERSTITIAL_TAXIGORD_V1.startsWith(publisherPrefix))
        assertTrue(AdMobUnits.INTERSTITIAL_TAXOBETAPUB.startsWith(publisherPrefix))
        assertTrue(AdMobUnits.BANNER_VIP.startsWith(publisherPrefix))
        assertTrue(AdMobUnits.REWARDED_PRESTAMEVIP.startsWith(publisherPrefix))
        assertTrue(AdMobUnits.INTERSTITIAL_SECURITY.startsWith(publisherPrefix))
        assertTrue(AdMobUnits.INTERSTITIAL_OPTIMIZER.startsWith(publisherPrefix))
        assertTrue(AdMobUnits.INTERSTITIAL_VAULT.startsWith(publisherPrefix))
        assertTrue(AdMobUnits.INTERSTITIAL_AI.startsWith(publisherPrefix))
    }
}
