package hr.doda.vedra.data.parser

import hr.doda.vedra.data.parser.ParsingUtils.cleanText
import hr.doda.vedra.data.parser.ParsingUtils.parseDmyDate
import hr.doda.vedra.data.parser.ParsingUtils.parseDouble
import hr.doda.vedra.data.parser.ParsingUtils.parseInt
import hr.doda.vedra.data.xml.parseXml
import hr.doda.vedra.domain.city.City
import hr.doda.vedra.domain.city.Coordinates
import hr.doda.vedra.domain.observation.CurrentObservation
import hr.doda.vedra.domain.observation.CurrentObservationSnapshot
import hr.doda.vedra.domain.observation.WindDirection

/** Parses `hrvatska_n.xml` / `hrvatska1_n.xml` (current observations). */
object CurrentObservationsParser {
    fun parse(xml: String): CurrentObservationSnapshot {
        val root = parseXml(xml)
        val termHeader = root.child("DatumTermin")
        val date =
            parseDmyDate(termHeader?.textOf("Datum"))
                ?: error("Missing <Datum> in current observations XML")
        val term = parseInt(termHeader?.textOf("Termin")) ?: 0

        val observations =
            root.children("Grad").map { gradNode ->
                val name = gradNode.textOf("GradIme").orEmpty().trim()
                val lat = parseDouble(gradNode.textOf("Lat"))
                val lon = parseDouble(gradNode.textOf("Lon"))
                val automatic = gradNode.attr("autom") == "1"
                val data = gradNode.child("Podatci")
                CurrentObservation(
                    city =
                        City(
                            name = name,
                            coordinates = if (lat != null && lon != null) Coordinates(lat, lon) else null,
                            automatic = automatic,
                        ),
                    temperatureC = parseDouble(data?.textOf("Temp")),
                    humidityPct = parseInt(data?.textOf("Vlaga")),
                    pressureHpa = parseDouble(data?.textOf("Tlak")),
                    pressureTendencyHpa = parseDouble(data?.textOf("TlakTend")),
                    windDirection = WindDirection.fromToken(data?.textOf("VjetarSmjer")),
                    windSpeedMs = parseDouble(data?.textOf("VjetarBrzina")),
                    weatherText = cleanText(data?.textOf("Vrijeme")),
                    weatherSymbol = cleanText(data?.textOf("VrijemeZnak")),
                )
            }

        return CurrentObservationSnapshot(date = date, termHour = term, observations = observations)
    }
}
