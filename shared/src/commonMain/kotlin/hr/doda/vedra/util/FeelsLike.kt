package hr.doda.vedra.util

import kotlin.math.exp
import kotlin.math.pow

/** Apparent / "feels like" temperature utilities. */
object FeelsLike {

    /**
     * Returns "apparent" temperature in °C using the Australian Bureau of
     * Meteorology formula (works at all temperature ranges, blends the
     * heat-index and wind-chill behaviours).
     *
     * Inputs:
     *  - [temperatureC]: ambient air temperature in °C
     *  - [humidityPct]: relative humidity in 0..100
     *  - [windSpeedMs]: wind speed at 10 m in m/s (DHMZ uses m/s)
     */
    fun apparentTemperature(
        temperatureC: Double,
        humidityPct: Int,
        windSpeedMs: Double,
    ): Double {
        val rh = humidityPct.coerceIn(0, 100) / 100.0
        // Vapour pressure (hPa).
        val e = rh * 6.105 * exp((17.27 * temperatureC) / (237.7 + temperatureC))
        return temperatureC + 0.33 * e - 0.70 * windSpeedMs - 4.00
    }

    /** US NWS heat index in °C — only meaningful at warm temperatures (>= 27 °C). */
    fun heatIndex(temperatureC: Double, humidityPct: Int): Double {
        if (temperatureC < 27.0) return temperatureC
        val tF = temperatureC * 9.0 / 5.0 + 32.0
        val rh = humidityPct.toDouble().coerceIn(0.0, 100.0)
        val hiF = -42.379 +
            2.04901523 * tF +
            10.14333127 * rh -
            0.22475541 * tF * rh -
            0.00683783 * tF.pow(2) -
            0.05481717 * rh.pow(2) +
            0.00122874 * tF.pow(2) * rh +
            0.00085282 * tF * rh.pow(2) -
            0.00000199 * tF.pow(2) * rh.pow(2)
        return (hiF - 32.0) * 5.0 / 9.0
    }

    /** Wind-chill in °C — only meaningful below 10 °C and above 1.34 m/s. */
    fun windChill(temperatureC: Double, windSpeedMs: Double): Double {
        if (temperatureC > 10.0 || windSpeedMs <= 1.34) return temperatureC
        val v = windSpeedMs * 3.6 // km/h
        return 13.12 + 0.6215 * temperatureC - 11.37 * v.pow(0.16) +
            0.3965 * temperatureC * v.pow(0.16)
    }
}
