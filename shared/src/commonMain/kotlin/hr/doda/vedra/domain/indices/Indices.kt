package hr.doda.vedra.domain.indices

import kotlinx.datetime.LocalDate

/** Hourly UV index measurements per station (`uvi.xml`). */
data class UvIndexReading(
    val hour: Int,
    val value: Double,
    val color: String?,
)

data class UvIndexStation(
    val name: String,
    val readings: List<UvIndexReading>,
)

data class UvIndexSnapshot(
    val date: LocalDate,
    val termHours: List<Int>,
    val stations: List<UvIndexStation>,
)

/** Bio-meteorological forecast (`bio_novo.xml`). */
data class BioForecastDay(
    val date: LocalDate,
    val text: String,
    /** Region key -> 1..3 burden level. */
    val regionLevels: Map<String, Int>,
)

data class BioForecast(
    val generatedAt: String,
    val days: List<BioForecastDay>,
)

/** Fire danger index per station (`indeks.xml`). */
data class FireDangerStation(
    val code: String,
    val name: String,
    val temperatureC: Double?,
    val humidityPct: Int?,
    val windMs: Double?,
    val precipitationMm: Double?,
    val ffmc: Int?,
    val dmc: Int?,
    val dc: Int?,
    val isi: Int?,
    val bui: Int?,
    val fwi: Int?,
    val dangerCategory: String,
)

data class FireDangerSnapshot(
    val date: LocalDate,
    val stations: List<FireDangerStation>,
)

/**
 * Heat or cold wave classification per major city (`toplinskival_5.xml`,
 * `hladnival.xml`).
 *
 * Day codes used by DHMZ:
 *  - "G" / "W" / "Y" / "R" — green / yellow-warm / amber / red severity.
 */
data class HeatColdWaveCity(
    val name: String,
    val coordinates: hr.doda.vedra.domain.city.Coordinates?,
    /** day1..dayN values, in order. */
    val dayLevels: List<String>,
)

data class HeatColdWaveSnapshot(
    val kind: Kind,
    val cities: List<HeatColdWaveCity>,
) {
    enum class Kind { HEAT, COLD }
}
