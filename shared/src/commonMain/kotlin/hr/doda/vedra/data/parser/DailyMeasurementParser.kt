package hr.doda.vedra.data.parser

import hr.doda.vedra.data.parser.ParsingUtils.cleanText
import hr.doda.vedra.data.parser.ParsingUtils.parseDmyDate
import hr.doda.vedra.data.parser.ParsingUtils.parseDouble
import hr.doda.vedra.data.parser.ParsingUtils.parseInt
import hr.doda.vedra.data.xml.parseXml
import hr.doda.vedra.domain.observation.DailyStationMeasurement
import hr.doda.vedra.domain.observation.DailyStationMeasurements

/**
 * Parses simple per-station daily measurement files: `tn.xml`, `tx.xml`,
 * `t5.xml`, `oborina.xml`. Each file contains a `<datumtermin>` block and
 * a flat list of `<grad>` entries.
 */
object DailyMeasurementParser {

    fun parseMinTemperature(xml: String): DailyStationMeasurements =
        parse(xml, valueTag = "tempmin", kind = DailyStationMeasurements.Kind.TEMP_MIN_C)

    fun parseMaxTemperature(xml: String): DailyStationMeasurements =
        parse(xml, valueTag = "tempmax", kind = DailyStationMeasurements.Kind.TEMP_MAX_C)

    fun parseGroundTemperature(xml: String): DailyStationMeasurements =
        parse(xml, valueTag = "temp5", kind = DailyStationMeasurements.Kind.TEMP_GROUND_C)

    fun parsePrecipitation(xml: String): DailyStationMeasurements =
        parse(xml, valueTag = "kolicina", kind = DailyStationMeasurements.Kind.PRECIPITATION_MM)

    private fun parse(xml: String, valueTag: String, kind: DailyStationMeasurements.Kind): DailyStationMeasurements {
        val root = parseXml(xml)
        val header = root.child("datumtermin")
        val date = parseDmyDate(header?.textOf("datum"))
            ?: error("Missing date in measurement XML")
        val term = parseInt(header?.textOf("termin")) ?: 0
        val values = root.children("grad").map { grad ->
            DailyStationMeasurement(
                cityName = cleanText(grad.textOf("ime")).orEmpty(),
                value = parseDouble(grad.textOf(valueTag)),
            )
        }
        return DailyStationMeasurements(
            date = date,
            termHour = term,
            kind = kind,
            values = values,
        )
    }
}
