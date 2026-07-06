package hr.doda.vedra.domain.observation

import hr.doda.vedra.domain.city.City
import kotlinx.datetime.LocalDate

/**
 * Wind direction as published by DHMZ. `C` in the XML means calm.
 * [fromToken] returns null for missing/unknown tokens.
 */
enum class WindDirection(val abbreviation: String) {
    NORTH("N"),
    NORTHEAST("NE"),
    EAST("E"),
    SOUTHEAST("SE"),
    SOUTH("S"),
    SOUTHWEST("SW"),
    WEST("W"),
    NORTHWEST("NW"),
    CALM("C");

    companion object {
        fun fromToken(token: String?): WindDirection? {
            val t = token?.trim()?.uppercase() ?: return null
            return entries.firstOrNull { it.abbreviation == t }
        }
    }
}

/** A current observation from a single weather station (hrvatska_n / hrvatska1_n). */
data class CurrentObservation(
    val city: City,
    val temperatureC: Double?,
    val humidityPct: Int?,
    val pressureHpa: Double?,
    val pressureTendencyHpa: Double?,
    val windDirection: WindDirection?,
    val windSpeedMs: Double?,
    val weatherText: String?,
    val weatherSymbol: String?,
)

/** Snapshot of all current observations across Croatia for a given timestamp. */
data class CurrentObservationSnapshot(
    val date: LocalDate,
    val termHour: Int,
    val observations: List<CurrentObservation>,
)

/** Daily aggregate from `tn.xml`, `tx.xml`, `t5.xml`, `oborina.xml`. */
data class DailyStationMeasurement(
    val cityName: String,
    val value: Double?,
)

data class DailyStationMeasurements(
    val date: LocalDate,
    val termHour: Int,
    val kind: Kind,
    val values: List<DailyStationMeasurement>,
) {
    /** What is measured (named Kind, not Unit, to avoid shadowing kotlin.Unit). */
    enum class Kind { TEMP_MIN_C, TEMP_MAX_C, TEMP_GROUND_C, PRECIPITATION_MM }
}
