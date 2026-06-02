package hr.doda.vedra.util

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertTrue

class FeelsLikeTest {

    @Test
    fun apparentTemperatureIsLowerWithWind() {
        val calm = FeelsLike.apparentTemperature(20.0, 60, 0.0)
        val windy = FeelsLike.apparentTemperature(20.0, 60, 8.0)
        assertTrue(windy < calm, "wind should reduce apparent temp")
    }

    @Test
    fun heatIndexAboveAirTemperatureWhenHumid() {
        val hi = FeelsLike.heatIndex(32.0, 80)
        assertTrue(hi > 32.0, "heat index should exceed air temp at high humidity")
    }

    @Test
    fun windChillBelowAirTemperatureWhenCold() {
        val wc = FeelsLike.windChill(0.0, 5.0)
        assertTrue(wc < 0.0)
    }

    @Test
    fun heatIndexNoOpAtCoolTemperatures() {
        val t = 20.0
        assertTrue(abs(FeelsLike.heatIndex(t, 60) - t) < 1e-9)
    }
}
