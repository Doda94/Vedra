package hr.doda.vedra.domain.observation

import hr.doda.vedra.domain.city.City
import kotlinx.datetime.LocalDate

/**
 * Wind direction abbreviation as published by DHMZ
 * (N, NE, E, SE, S, SW, W, NW; or "C" for calm; null if missing).
 */
typealias WindDirection = String

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
    val unit: Unit,
    val values: List<DailyStationMeasurement>,
) {
    enum class Unit { TEMP_MIN_C, TEMP_MAX_C, TEMP_GROUND_C, PRECIPITATION_MM }
}
