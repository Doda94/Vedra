package hr.doda.vedra.data.parser

import hr.doda.vedra.data.parser.ParsingUtils.cleanText
import hr.doda.vedra.data.parser.ParsingUtils.parseDmyDate
import hr.doda.vedra.data.parser.ParsingUtils.parseDouble
import hr.doda.vedra.data.parser.ParsingUtils.parseInt
import hr.doda.vedra.data.xml.parseXml
import hr.doda.vedra.domain.indices.BioForecast
import hr.doda.vedra.domain.indices.BioForecastDay
import hr.doda.vedra.domain.indices.FireDangerSnapshot
import hr.doda.vedra.domain.indices.FireDangerStation
import hr.doda.vedra.domain.indices.HeatColdWaveCity
import hr.doda.vedra.domain.indices.HeatColdWaveSnapshot
import hr.doda.vedra.domain.indices.UvIndexReading
import hr.doda.vedra.domain.indices.UvIndexSnapshot
import hr.doda.vedra.domain.indices.UvIndexStation

/** Parses `uvi.xml`. The first `<Podatci>` block holds the column headers. */
object UvIndexParser {

    fun parse(xml: String): UvIndexSnapshot {
        val root = parseXml(xml)
        val date = parseDmyDate(root.textOf("Datum"))
            ?: error("Missing <Datum> in uvi.xml")
        val rows = root.children("Podatci")
        val header = rows.firstOrNull() ?: error("Missing UV header row")
        val termHours = header.children("Termin").mapNotNull { parseInt(it.text) }

        val stations = rows.drop(1).mapNotNull { row ->
            val name = cleanText(row.textOf("Postaja")) ?: return@mapNotNull null
            val readings = row.children("Termin").mapIndexedNotNull { idx, t ->
                val v = parseDouble(t.text) ?: return@mapIndexedNotNull null
                UvIndexReading(
                    hour = termHours.getOrElse(idx) { idx },
                    value = v,
                    color = t.attr("boja"),
                )
            }
            UvIndexStation(name = name, readings = readings)
        }

        return UvIndexSnapshot(date = date, termHours = termHours, stations = stations)
    }
}

/** Parses `bio_novo.xml`. */
object BioForecastParser {

    fun parse(xml: String): BioForecast {
        val root = parseXml(xml)
        val days = root.children("Podaci").map { day ->
            BioForecastDay(
                date = parseDmyDate(day.textOf("Datum"))
                    ?: error("Missing <Datum> in bio_novo.xml"),
                text = cleanText(day.textOf("Tekst")).orEmpty(),
                regionLevels = day.children("station").mapNotNull { st ->
                    val name = st.attr("name") ?: return@mapNotNull null
                    val level = parseInt(st.text) ?: return@mapNotNull null
                    name to level
                }.toMap(),
            )
        }
        return BioForecast(
            generatedAt = cleanText(root.textOf("Prognozirano")).orEmpty(),
            days = days,
        )
    }
}

/** Parses `indeks.xml` (fire danger / FWI system). */
object FireDangerParser {

    fun parse(xml: String): FireDangerSnapshot {
        val root = parseXml(xml)
        val date = parseDmyDate(root.textOf("datum"))
            ?: error("Missing <datum> in indeks.xml")
        val stations = root.children("postaja").map { p ->
            FireDangerStation(
                code = cleanText(p.textOf("code")).orEmpty(),
                name = cleanText(p.textOf("ime")).orEmpty(),
                temperatureC = parseDouble(p.textOf("temperatura")),
                humidityPct = parseInt(p.textOf("vlaga")),
                windMs = parseDouble(p.textOf("vjetar")),
                precipitationMm = parseDouble(p.textOf("oborina")),
                ffmc = parseInt(p.textOf("ffmc")),
                dmc = parseInt(p.textOf("dmc")),
                dc = parseInt(p.textOf("dc")),
                isi = parseInt(p.textOf("isi")),
                bui = parseInt(p.textOf("bui")),
                fwi = parseInt(p.textOf("fwi")),
                dangerCategory = cleanText(p.textOf("indeks")).orEmpty(),
            )
        }
        return FireDangerSnapshot(date = date, stations = stations)
    }
}

/** Parses `toplinskival_5.xml` and `hladnival.xml`. */
object HeatColdWaveParser {

    fun parseHeat(xml: String): HeatColdWaveSnapshot =
        parse(xml, HeatColdWaveSnapshot.Kind.HEAT)

    fun parseCold(xml: String): HeatColdWaveSnapshot =
        parse(xml, HeatColdWaveSnapshot.Kind.COLD)

    private fun parse(xml: String, kind: HeatColdWaveSnapshot.Kind): HeatColdWaveSnapshot {
        val root = parseXml(xml)
        val section = root.child("section") ?: error("Missing <section>")
        val cities = section.children("station").map { st ->
            val params = st.children("param")
                .associate { (it.attr("name") ?: "") to (it.attr("value") ?: "") }
            val dayKeys = params.keys.filter { it.startsWith("dan") }
                .sortedBy { it.removePrefix("dan").toIntOrNull() ?: 0 }
            HeatColdWaveCity(
                name = st.attr("name").orEmpty(),
                coordinates = run {
                    val lat = parseDouble(st.attr("lat"))
                    val lon = parseDouble(st.attr("lon"))
                    if (lat != null && lon != null) {
                        hr.doda.vedra.domain.city.Coordinates(lat, lon)
                    } else null
                },
                dayLevels = dayKeys.mapNotNull { params[it] },
            )
        }
        return HeatColdWaveSnapshot(kind = kind, cities = cities)
    }
}
