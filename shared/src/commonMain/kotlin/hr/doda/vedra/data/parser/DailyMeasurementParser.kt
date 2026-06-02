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
        parse(xml, valueTag = "tempmin", unit = DailyStationMeasurements.Unit.TEMP_MIN_C)

    fun parseMaxTemperature(xml: String): DailyStationMeasurements =
        parse(xml, valueTag = "tempmax", unit = DailyStationMeasurements.Unit.TEMP_MAX_C)

    fun parseGroundTemperature(xml: String): DailyStationMeasurements =
        parse(xml, valueTag = "temp5", unit = DailyStationMeasurements.Unit.TEMP_GROUND_C)

    fun parsePrecipitation(xml: String): DailyStationMeasurements =
        parse(xml, valueTag = "kolicina", unit = DailyStationMeasurements.Unit.PRECIPITATION_MM)

    private fun parse(xml: String, valueTag: String, unit: DailyStationMeasurements.Unit): DailyStationMeasurements {
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
            unit = unit,
            values = values,
        )
    }
}
