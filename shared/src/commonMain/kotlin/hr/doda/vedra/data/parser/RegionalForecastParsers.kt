package hr.doda.vedra.data.parser

import hr.doda.vedra.data.parser.ParsingUtils.cleanText
import hr.doda.vedra.data.parser.ParsingUtils.parseDdmmyyDate
import hr.doda.vedra.data.parser.ParsingUtils.parseInt
import hr.doda.vedra.data.xml.XmlNode
import hr.doda.vedra.data.xml.parseXml
import hr.doda.vedra.domain.city.Region
import hr.doda.vedra.domain.forecast.NationalDailyForecast
import hr.doda.vedra.domain.forecast.OutlookForecast
import hr.doda.vedra.domain.forecast.RegionalDailyForecast
import hr.doda.vedra.domain.forecast.RegionalDescriptions

/** Parses `prognoza_danas.xml` and `prognoza_sutra.xml`. */
object NationalDailyForecastParser {

    fun parse(xml: String): NationalDailyForecast {
        val root = parseXml(xml)
        val section = root.child("section") ?: error("Missing <section>")
        val params = section.params()
        val dateStr = params["datum"]
        val date = parseDdmmyyDate(dateStr) ?: error("Bad <param datum=\"$dateStr\">")

        val regions = section.children("station").map { st -> parseRegion(st, fallbackDate = date) }
        return NationalDailyForecast(
            date = date,
            regions = regions,
            croatiaSummary = cleanText(params["rh_text"]).orEmpty(),
            zagrebSummary = cleanText(params["zg_text"]).orEmpty(),
        )
    }
}

/** Parses `prognoza_izgledi.xml` (multi-day outlook). */
object OutlookForecastParser {

    fun parse(xml: String): OutlookForecast {
        val root = parseXml(xml)
        val section = root.child("section") ?: error("Missing <section>")
        val params = section.params()
        val days = section.children("station").map { parseRegion(it, fallbackDate = null) }
        return OutlookForecast(
            summary = cleanText(params["rh_text"]).orEmpty(),
            days = days,
        )
    }
}

private fun parseRegion(station: XmlNode, fallbackDate: kotlinx.datetime.LocalDate?): RegionalDailyForecast {
    val key = station.attr("name").orEmpty()
    val params = station.params()
    val date = parseDdmmyyDate(params["datum"]) ?: fallbackDate
        ?: error("Region $key has no <param datum>")
    val regionKey = key.substringBefore(":").let { rest ->
        if (key.startsWith("hr.izgledi:")) key.substringAfter(":") else rest
    }
    return RegionalDailyForecast(
        date = date,
        region = Region.fromKey(regionKey)
            ?: Region.fromKey(stripOutlookKey(regionKey)),
        rawRegionKey = regionKey,
        tempMinC = parseInt(params["Tmn"]),
        tempMaxC = parseInt(params["Tmx"]),
        symbol = cleanText(params["vrijeme"]),
        windCode = cleanText(params["wind"]),
        warningLevel = parseInt(params["pozor"]),
    )
}

private fun stripOutlookKey(s: String): String =
    s.removePrefix("Kopno").removePrefix("More").trimEnd { it.isDigit() }

private fun XmlNode.params(): Map<String, String> =
    children("param").associate { (it.attr("name") ?: "") to (it.attr("value") ?: "") }

/** Parses `regije_danas.xml` (free-form regional descriptions). */
object RegionalDescriptionsParser {

    private val REGION_TAGS = mapOf(
        "istocna" to Region.EASTERN,
        "sredisnja" to Region.CENTRAL,
        "sjjadran" to Region.NORTHERN_ADRIATIC,
        "gorska" to Region.MOUNTAINOUS,
        "dalmacija" to Region.DALMATIA,
        "istra" to Region.ISTRIA,
        "sjjadran_more" to Region.NORTHERN_ADRIATIC,
    )

    fun parse(xml: String): RegionalDescriptions {
        val root = parseXml(xml)
        val date = ParsingUtils.parseDmyDate(root.textOf("datum"))
            ?: error("Missing <datum>")
        val descriptions = REGION_TAGS.mapNotNull { (tag, region) ->
            val text = cleanText(root.textOf(tag)) ?: return@mapNotNull null
            region to text
        }.toMap()
        return RegionalDescriptions(date = date, descriptions = descriptions)
    }
}
