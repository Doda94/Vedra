package hr.doda.vedra.data.parser

import hr.doda.vedra.data.parser.ParsingUtils.cleanText
import hr.doda.vedra.data.xml.XmlNode
import hr.doda.vedra.data.xml.parseXml
import hr.doda.vedra.domain.alert.AlertSeverity
import hr.doda.vedra.domain.alert.AlertType
import hr.doda.vedra.domain.alert.HydroBulletin
import hr.doda.vedra.domain.alert.MeteoAlert
import hr.doda.vedra.domain.alert.MeteoAlertInfo
import hr.doda.vedra.domain.alert.MeteoAlertSet
import kotlinx.datetime.Instant

/** Parses CAP 1.2 alert files (`cap_hr_today.xml` etc.). */
object MeteoAlertParser {

    fun parse(xml: String, target: MeteoAlertSet.Target = MeteoAlertSet.Target.TODAY): MeteoAlertSet {
        val root = parseXml(xml)
        // Each file contains exactly one <alert>; root *is* that alert.
        return MeteoAlertSet(target = target, alerts = listOf(parseAlert(root)))
    }

    private fun parseAlert(alert: XmlNode): MeteoAlert {
        val infos = alert.children("info").map { parseInfo(it) }
        return MeteoAlert(
            identifier = alert.textOf("identifier").orEmpty(),
            sender = alert.textOf("sender").orEmpty(),
            sent = parseInstant(alert.textOf("sent")),
            msgType = alert.textOf("msgType").orEmpty(),
            infos = infos,
        )
    }

    private fun parseInfo(info: XmlNode): MeteoAlertInfo {
        val params = info.children("parameter")
            .associate {
                (it.textOf("valueName") ?: "") to (it.textOf("value") ?: "")
            }
        val areas = info.children("area")
        return MeteoAlertInfo(
            language = info.textOf("language").orEmpty(),
            event = info.textOf("event").orEmpty(),
            severity = AlertSeverity.fromAwarenessLevel(params["awareness_level"])
                .takeIf { it != AlertSeverity.UNKNOWN }
                ?: AlertSeverity.fromName(info.textOf("severity")),
            type = AlertType.fromAwarenessType(params["awareness_type"]),
            onset = parseInstant(info.textOf("onset")),
            expires = parseInstant(info.textOf("expires")),
            description = cleanText(info.textOf("description")).orEmpty(),
            instruction = cleanText(info.textOf("instruction")).orEmpty(),
            areas = areas.mapNotNull { cleanText(it.textOf("areaDesc")) },
            regionCodes = areas.flatMap { area ->
                area.children("geocode")
                    .filter { it.textOf("valueName")?.trim().equals("EMMA_ID", ignoreCase = true) }
                    .mapNotNull { cleanText(it.textOf("value")) }
            },
        )
    }

    private fun parseInstant(raw: String?): Instant? =
        cleanText(raw)?.let { runCatching { Instant.parse(it) }.getOrNull() }
}

/** Parses `hidro_bilten.xml`. */
object HydroBulletinParser {
    fun parse(xml: String): HydroBulletin {
        val root = parseXml(xml)
        val rivers = listOf("sava", "kupa", "dunav", "mura", "drava", "neretva")
            .mapNotNull { tag ->
                cleanText(root.textOf(tag))?.let { tag to it }
            }
            .toMap()
        return HydroBulletin(
            period = cleanText(root.textOf("period_prognoze")).orEmpty(),
            recordedAt = listOfNotNull(
                cleanText(root.textOf("datum_upisa")),
                cleanText(root.textOf("vrijeme_upisa")),
            ).joinToString(" "),
            warning = cleanText(root.textOf("upozorenje")).orEmpty(),
            rivers = rivers,
        )
    }
}
