package hr.doda.vedra.data.parser

import hr.doda.vedra.data.parser.ParsingUtils.cleanText
import hr.doda.vedra.data.parser.ParsingUtils.parseDmyDate
import hr.doda.vedra.data.parser.ParsingUtils.parseDouble
import hr.doda.vedra.data.parser.ParsingUtils.parseInt
import hr.doda.vedra.data.xml.parseXml
import hr.doda.vedra.domain.europe.EuropeanCityWeather
import hr.doda.vedra.domain.europe.EuropeanWeatherSnapshot

/** Parses `europa_n.xml` (current weather across European cities). */
object EuropeanWeatherParser {

    fun parse(xml: String): EuropeanWeatherSnapshot {
        val root = parseXml(xml)
        val termHeader = root.child("DatumTermin")
        val date = parseDmyDate(termHeader?.textOf("Datum"))
            ?: error("Missing <Datum> in europa_n.xml")
        val term = parseInt(termHeader?.textOf("Termin")) ?: 0

        val cities = root.children("Grad").map { grad ->
            val data = grad.child("Podatci")
            EuropeanCityWeather(
                name = cleanText(grad.textOf("GradIme")).orEmpty(),
                temperatureC = parseInt(data?.textOf("Temp")),
                humidityPct = parseInt(data?.textOf("Vlaga")),
                pressureHpa = parseDouble(data?.textOf("Tlak")),
                pressureTendencyHpa = parseDouble(data?.textOf("TlakTend")),
                windDirection = cleanText(data?.textOf("VjetarSmjer")),
                windSpeedMs = parseDouble(data?.textOf("VjetarBrzina")),
                weatherText = cleanText(data?.textOf("Vrijeme")),
                weatherSymbol = cleanText(data?.textOf("VrijemeZnak")),
            )
        }
        return EuropeanWeatherSnapshot(date = date, termHour = term, cities = cities)
    }
}
