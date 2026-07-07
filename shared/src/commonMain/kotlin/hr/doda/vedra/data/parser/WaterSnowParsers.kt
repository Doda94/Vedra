package hr.doda.vedra.data.parser

import hr.doda.vedra.data.parser.ParsingUtils.cleanText
import hr.doda.vedra.data.parser.ParsingUtils.parseDouble
import hr.doda.vedra.data.parser.ParsingUtils.parseInt
import hr.doda.vedra.data.xml.XmlNode
import hr.doda.vedra.data.xml.parseXml
import hr.doda.vedra.domain.hydro.WaterTemperatureReading
import hr.doda.vedra.domain.hydro.WaterTemperatureSeries
import hr.doda.vedra.domain.hydro.WaterTemperatureSnapshot
import hr.doda.vedra.domain.observation.SnowDepth
import hr.doda.vedra.domain.observation.SnowDepthSnapshot
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Parses `temp_vode.xml` — a KiTS timeseries export of hourly-averaged
 * water temperatures at hydrological stations. Layout per `<timeseries>`:
 * a `<timeseriesPath>` like `/tsm(7000/7012/WT/Cmd.O);average(1 hour)`
 * and `<data>` rows of `<r><c>timestamp</c><c>value</c><c>status</c><c>interpolation</c></r>`.
 */
object WaterTemperatureParser {
    fun parse(xml: String): WaterTemperatureSnapshot {
        val root = parseXml(xml)
        val series =
            root.children("timeseries").mapNotNull { ts ->
                val path = ts.child("basic_data")?.textOf("timeseriesPath") ?: return@mapNotNull null
                val codes = path.substringAfter("tsm(", "").substringBefore(")").split('/')
                val readings =
                    ts.child("data")?.children("r").orEmpty().mapNotNull { row ->
                        val cells = row.children("c")
                        val time =
                            cleanText(cells.getOrNull(0)?.text)
                                ?.let { runCatching { Instant.parse(it) }.getOrNull() }
                                ?: return@mapNotNull null
                        val value = parseDouble(cells.getOrNull(1)?.text) ?: return@mapNotNull null
                        WaterTemperatureReading(time = time, temperatureC = value)
                    }
                WaterTemperatureSeries(
                    groupCode = codes.getOrElse(0) { "" },
                    stationCode = codes.getOrElse(1) { "" },
                    readings = readings,
                )
            }
        return WaterTemperatureSnapshot(series = series)
    }
}

/**
 * Parses `snijeg_n.xml` (snow depths). Outside the snow season the file
 * contains only `<naslov>` and the station list is empty.
 *
 * CAVEAT: no winter fixture is available yet, so the per-station tags are
 * a best guess based on other DHMZ files (`<grad>` with `<ime>` +
 * `<visina>`/`<vrijednost>`). Re-verify against a real winter file.
 */
object SnowDepthParser {
    fun parse(xml: String): SnowDepthSnapshot {
        val root = parseXml(xml)
        val title = cleanText(root.textOf("naslov")).orEmpty()
        val stations =
            root.children("grad").mapNotNull { parseStation(it) } +
                root.children("postaja").mapNotNull { parseStation(it) }
        return SnowDepthSnapshot(
            title = title,
            date = titleDate(title),
            hour = titleHour(title),
            stations = stations,
        )
    }

    private fun parseStation(node: XmlNode): SnowDepth? {
        val name =
            cleanText(node.textOf("ime"))
                ?: cleanText(node.textOf("GradIme"))
                ?: cleanText(node.attr("ime"))
                ?: return null
        val depth =
            parseDouble(node.textOf("visina"))
                ?: parseDouble(node.textOf("vrijednost"))
                ?: parseDouble(node.textOf("snijeg"))
        return SnowDepth(stationName = name, depthCm = depth)
    }

    /** Extracts dd.MM.yyyy from a title like "Visine snijega u Hrvatskoj 02.06.2026 u 08 h". */
    private fun titleDate(title: String): LocalDate? {
        val match = Regex("""(\d{1,2})\.(\d{1,2})\.(\d{4})""").find(title) ?: return null
        val (d, m, y) = match.destructured
        return runCatching { LocalDate(y.toInt(), m.toInt(), d.toInt()) }.getOrNull()
    }

    /** Extracts the hour from "... u 08 h". */
    private fun titleHour(title: String): Int? =
        Regex("""u\s+(\d{1,2})\s*h""")
            .find(title)
            ?.groupValues
            ?.get(1)
            ?.let { parseInt(it) }
}
