package hr.doda.vedra.data.parser

import hr.doda.vedra.data.parser.ParsingUtils.cleanText
import hr.doda.vedra.data.parser.ParsingUtils.parseDateAtHour
import hr.doda.vedra.data.parser.ParsingUtils.parseDmyDate
import hr.doda.vedra.data.parser.ParsingUtils.parseDouble
import hr.doda.vedra.data.parser.ParsingUtils.parseInt
import hr.doda.vedra.data.xml.XmlNode
import hr.doda.vedra.data.xml.parseXml
import hr.doda.vedra.domain.forecast.CityForecast
import hr.doda.vedra.domain.forecast.DailyForecast
import hr.doda.vedra.domain.forecast.HourlyForecast

/**
 * Parses `7d_graf_i_simboli.xml` — 7-day hourly forecast for ~324 Croatian
 * locations. This is the most data-rich and important DHMZ XML.
 */
object SevenDayForecastParser {

    /** Parse all cities. Returns one [CityForecast] per `<grad>` element. */
    fun parse(xml: String): List<CityForecast> {
        val root = parseXml(xml)
        return root.children("grad").map { parseCity(it) }
    }

    /** Parse only the city whose `ime` or `code` matches [cityNameOrCode]. */
    fun parseCity(xml: String, cityNameOrCode: String): CityForecast? {
        val root = parseXml(xml)
        val target = root.children("grad").firstOrNull {
            it.attr("ime").equals(cityNameOrCode, ignoreCase = true) ||
                it.attr("code").equals(cityNameOrCode, ignoreCase = true)
        } ?: return null
        return parseCity(target)
    }

    private fun parseCity(grad: XmlNode): CityForecast {
        val name = grad.attr("ime").orEmpty()
        val code = grad.attr("code") ?: name
        val hourly = grad.children("dan").mapNotNull { dan ->
            val date = parseDmyDate(dan.attr("datum")) ?: return@mapNotNull null
            val hour = parseInt(dan.attr("sat")) ?: return@mapNotNull null
            HourlyForecast(
                time = parseDateAtHour(date, hour),
                temperatureC = parseDouble(dan.textOf("t_2m")),
                symbol = cleanText(dan.textOf("simbol")),
                wind = cleanText(dan.textOf("vjetar")),
                precipitationMm = parseDouble(dan.textOf("oborina")),
                precipitationProbabilityPct = parseInt(dan.textOf("vjerojatnost")),
            )
        }
        return CityForecast(
            cityName = name,
            cityCode = code,
            hourly = hourly,
            daily = aggregateDaily(hourly),
        )
    }

    /** Build daily summaries from hourly slots. */
    fun aggregateDaily(hourly: List<HourlyForecast>): List<DailyForecast> =
        hourly.groupBy { it.time.date }.map { (date, slots) ->
            val temps = slots.mapNotNull { it.temperatureC }
            val precSum = slots.mapNotNull { it.precipitationMm }
                .takeIf { it.isNotEmpty() }?.sum()
            val maxProb = slots.mapNotNull { it.precipitationProbabilityPct }.maxOrNull()
            // Pick the symbol seen during daylight (10..16) most often, falling back to overall mode.
            val daylight = slots.filter { it.time.hour in 10..16 }
            val symbol = (daylight.ifEmpty { slots })
                .mapNotNull { it.symbol }
                .groupingBy { stripNightSuffix(it) }
                .eachCount()
                .maxByOrNull { it.value }?.key
            DailyForecast(
                date = date,
                tempMinC = temps.minOrNull(),
                tempMaxC = temps.maxOrNull(),
                totalPrecipMm = precSum,
                maxPrecipProbabilityPct = maxProb,
                dominantSymbol = symbol,
            )
        }.sortedBy { it.date }

    private fun stripNightSuffix(symbol: String): String = symbol.removeSuffix("n")
}
