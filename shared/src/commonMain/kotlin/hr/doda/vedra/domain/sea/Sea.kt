package hr.doda.vedra.domain.sea

import kotlinx.datetime.LocalDate

/** Adriatic marine forecast block (`jadran_h.xml`). */
data class MarineForecast(
    val title: String,
    val warning: String?,
    val state: String?,
    val first12hForecast: String,
    val next12hForecast: String,
)

/** Marine forecast for sailors (`pomorci.xml`) — split per Adriatic region. */
data class SailorsMarineForecast(
    val title: String,
    val warning: String?,
    val state: String?,
    val validUntil: String,
    val regions: Map<String, String>,
)

/** Sea temperature observation. */
data class SeaTemperature(
    val stationName: String,
    /** Hour of day -> temperature in °C; missing values are dropped. */
    val readings: Map<Int, Double>,
)

data class SeaTemperatureSnapshot(
    val date: LocalDate,
    val termHours: List<Int>,
    val stations: List<SeaTemperature>,
)
