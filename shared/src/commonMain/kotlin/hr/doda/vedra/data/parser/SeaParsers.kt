package hr.doda.vedra.data.parser

import hr.doda.vedra.data.parser.ParsingUtils.cleanText
import hr.doda.vedra.data.parser.ParsingUtils.parseDmyDate
import hr.doda.vedra.data.parser.ParsingUtils.parseDouble
import hr.doda.vedra.data.parser.ParsingUtils.parseInt
import hr.doda.vedra.data.xml.parseXml
import hr.doda.vedra.domain.sea.MarineForecast
import hr.doda.vedra.domain.sea.SailorsMarineForecast
import hr.doda.vedra.domain.sea.SeaTemperature
import hr.doda.vedra.domain.sea.SeaTemperatureSnapshot

/** Parses `jadran_h.xml`. */
object MarineForecastParser {

    fun parse(xml: String): MarineForecast {
        val root = parseXml(xml)
        val texts = root.children("Prognoza_tekst")
            .mapNotNull { cleanText(it.text) }
        return MarineForecast(
            title = cleanText(root.textOf("Naslov")).orEmpty(),
            warning = cleanText(root.child("Upozorenje")?.textOf("Upozorenje_tekst")),
            state = cleanText(root.child("Stanje")?.textOf("Stanje_tekst")),
            first12hForecast = texts.getOrNull(0).orEmpty(),
            next12hForecast = texts.getOrNull(1).orEmpty(),
        )
    }
}

/** Parses `pomorci.xml`. */
object SailorsMarineForecastParser {

    fun parse(xml: String): SailorsMarineForecast {
        val root = parseXml(xml)
        val titles = root.children("Prognoza_naslov").mapNotNull { cleanText(it.text) }
        val texts = root.children("Prognoza_tekst").mapNotNull { cleanText(it.text) }
        val regions = titles.zip(texts).toMap()
        return SailorsMarineForecast(
            title = cleanText(root.textOf("Naslov")).orEmpty(),
            warning = cleanText(root.textOf("Upozorenje")),
            state = cleanText(root.textOf("Stanje")),
            validUntil = cleanText(root.textOf("Prognoza_zaglavlje")).orEmpty(),
            regions = regions,
        )
    }
}

/** Parses `more_n.xml`. */
object SeaTemperatureParser {

    fun parse(xml: String): SeaTemperatureSnapshot {
        val root = parseXml(xml)
        val date = parseDmyDate(root.textOf("Datum"))
            ?: error("Missing <Datum> in more_n.xml")
        val rows = root.children("Podatci")
        val header = rows.firstOrNull() ?: error("Missing header row in more_n.xml")
        val termHours = header.children("Termin").mapNotNull { parseInt(it.text) }

        val stations = rows.drop(1).map { row ->
            val name = cleanText(row.textOf("Postaja")).orEmpty()
            val readings = row.children("Termin").mapIndexedNotNull { idx, t ->
                val value = parseDouble(t.text) ?: return@mapIndexedNotNull null
                val hour = termHours.getOrElse(idx) { idx }
                hour to value
            }.toMap()
            SeaTemperature(stationName = name, readings = readings)
        }
        return SeaTemperatureSnapshot(date = date, termHours = termHours, stations = stations)
    }
}
