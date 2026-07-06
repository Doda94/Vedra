package hr.doda.vedra.domain.forecast

import hr.doda.vedra.domain.city.Region
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * One hourly forecast slot from `7d_graf_i_simboli.xml`.
 * Symbol may carry an `n` suffix for nighttime variants (e.g. "15n").
 *
 * [time] is implicit **Europe/Zagreb local time** as published by DHMZ —
 * there is no timezone in the XML. Convert via
 * `time.toInstant(TimeZone.of("Europe/Zagreb"))` before comparing with
 * `Clock.System.now()` ("now" markers), and mind DST transition days.
 */
data class HourlyForecast(
    val time: LocalDateTime,
    val temperatureC: Double?,
    /** DHMZ weather symbol code (e.g. "2", "15", "15n"). */
    val symbol: String?,
    /** Wind text token like "SW1", "N3", "C" (calm). */
    val wind: String?,
    val precipitationMm: Double?,
    val precipitationProbabilityPct: Int?,
)

/** Aggregated daily summary computed from an [HourlyForecast] list. */
data class DailyForecast(
    val date: LocalDate,
    val tempMinC: Double?,
    val tempMaxC: Double?,
    val totalPrecipMm: Double?,
    val maxPrecipProbabilityPct: Int?,
    val dominantSymbol: String?,
)

/** All hourly + daily forecast data for a single city/place from `7d_graf_i_simboli.xml`. */
data class CityForecast(
    val cityName: String,
    val cityCode: String,
    val hourly: List<HourlyForecast>,
    val daily: List<DailyForecast>,
)

/** Header / "today / tomorrow" summary as published in `prognoza_danas.xml` etc. */
data class RegionalDailyForecast(
    val date: LocalDate,
    val region: Region?,
    val rawRegionKey: String,
    val tempMinC: Int?,
    val tempMaxC: Int?,
    val symbol: String?,
    val windCode: String?,
    val warningLevel: Int? = null,
)

/** A `prognoza_danas` / `prognoza_sutra` file. */
data class NationalDailyForecast(
    val date: LocalDate,
    val regions: List<RegionalDailyForecast>,
    /** Long Croatian narrative summary (`rh_text`). */
    val croatiaSummary: String,
    /** Forecast specifically for Zagreb (`zg_text`). */
    val zagrebSummary: String,
)

/** Multi-day outlook (`prognoza_izgledi.xml`). */
data class OutlookForecast(
    val summary: String,
    val days: List<RegionalDailyForecast>,
)

/** Free-form regional descriptions from `regije_danas.xml`. */
data class RegionalDescriptions(
    val date: LocalDate,
    val descriptions: Map<Region, String>,
)
