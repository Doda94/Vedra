package hr.doda.vedra.domain.hydro

import kotlinx.datetime.Instant

/** One hourly-averaged water temperature reading from a hydrological station. */
data class WaterTemperatureReading(
    val time: Instant,
    val temperatureC: Double,
)

/**
 * Hourly water temperature series for one hydrological station from
 * `temp_vode.xml` (KiTS timeseries export).
 *
 * Stations are identified only by numeric codes embedded in the
 * timeseries path (e.g. `/tsm(7000/7012/WT/...)` → group "7000",
 * station "7012"). DHMZ does not publish a name mapping in this file;
 * a code→name table can be added in the UI layer if needed.
 */
data class WaterTemperatureSeries(
    val groupCode: String,
    val stationCode: String,
    val readings: List<WaterTemperatureReading>,
) {
    val latest: WaterTemperatureReading? get() = readings.maxByOrNull { it.time }
}

/** All water temperature series in one `temp_vode.xml` snapshot. */
data class WaterTemperatureSnapshot(
    val series: List<WaterTemperatureSeries>,
)
