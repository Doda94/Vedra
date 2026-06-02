package hr.doda.vedra.domain.europe

import kotlinx.datetime.LocalDate

/** Current weather observation for a European city (`europa_n.xml`). */
data class EuropeanCityWeather(
    val name: String,
    val temperatureC: Int?,
    val humidityPct: Int?,
    val pressureHpa: Double?,
    val pressureTendencyHpa: Double?,
    val windDirection: String?,
    val windSpeedMs: Double?,
    val weatherText: String?,
    val weatherSymbol: String?,
)

data class EuropeanWeatherSnapshot(
    val date: LocalDate,
    val termHour: Int,
    val cities: List<EuropeanCityWeather>,
)
