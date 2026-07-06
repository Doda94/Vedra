package hr.doda.vedra.data.parser

import hr.doda.vedra.domain.alert.AlertSeverity
import hr.doda.vedra.domain.alert.MeteoAlertSet
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * JVM-side smoke tests that load every bundled DHMZ XML directly from
 * disk and exercise the corresponding parser. Compose Resources is not
 * usable from plain Android unit tests, so we bypass it here and read
 * the fixtures straight from `composeResources/files/dhmz/`.
 */
class ParsersFixtureTest {

    private val fixturesDir = File("src/commonMain/composeResources/files/dhmz").also {
        check(it.exists()) {
            "fixtures dir not found at ${it.absolutePath}; run gradle from project root"
        }
    }

    private fun read(name: String): String = File(fixturesDir, name).readText()

    @Test
    fun parsesCurrentObservations() {
        val snap = CurrentObservationsParser.parse(read("hrvatska_n.xml"))
        assertTrue(snap.observations.isNotEmpty())
        val rcBilogora = snap.observations.firstOrNull { it.city.name.contains("Bilogora") }
        assertNotNull(rcBilogora)
        assertEquals(20.7, rcBilogora.temperatureC)
    }

    @Test
    fun parsesSevenDayForecast() {
        val cities = SevenDayForecastParser.parse(read("7d_graf_i_simboli.xml"))
        assertTrue(cities.size > 100, "expected many cities, got ${cities.size}")
        val bistra = cities.firstOrNull { it.cityName == "BISTRA" }
        assertNotNull(bistra)
        assertTrue(bistra.hourly.size > 24)
        assertTrue(bistra.daily.isNotEmpty())
        val firstDay = bistra.daily.first()
        assertNotNull(firstDay.tempMaxC)
        assertNotNull(firstDay.tempMinC)
    }

    @Test
    fun parsesNationalForecast() {
        val today = NationalDailyForecastParser.parse(read("prognoza_danas.xml"))
        assertTrue(today.regions.isNotEmpty())
        assertTrue(today.croatiaSummary.isNotEmpty())
        assertTrue(today.zagrebSummary.isNotEmpty())
    }

    @Test
    fun parsesOutlook() {
        val outlook = OutlookForecastParser.parse(read("prognoza_izgledi.xml"))
        assertTrue(outlook.summary.isNotEmpty())
        assertTrue(outlook.days.isNotEmpty())
    }

    @Test
    fun parsesRegionalDescriptions() {
        val descriptions = RegionalDescriptionsParser.parse(read("regije_danas.xml"))
        assertTrue(descriptions.descriptions.isNotEmpty())
    }

    @Test
    fun parsesAlerts() {
        val today = MeteoAlertParser.parse(read("cap_hr_today.xml"), MeteoAlertSet.Target.TODAY)
        assertTrue(today.alerts.isNotEmpty())
        val alert = today.alerts.first()
        assertTrue(alert.infos.isNotEmpty())
        val hr = alert.localized("hr")
        assertNotNull(hr)
        assertTrue(hr.severity != AlertSeverity.UNKNOWN)
        assertTrue(hr.regionCodes.isNotEmpty())
    }

    @Test
    fun parsesHydroBulletin() {
        val bulletin = HydroBulletinParser.parse(read("hidro_bilten.xml"))
        assertTrue(bulletin.warning.isNotEmpty())
        assertTrue(bulletin.rivers.isNotEmpty())
    }

    @Test
    fun parsesMarine() {
        val forecast = MarineForecastParser.parse(read("jadran_h.xml"))
        assertTrue(forecast.title.isNotEmpty())
        assertTrue(forecast.first12hForecast.isNotEmpty())
    }

    @Test
    fun parsesSailorsMarine() {
        val forecast = SailorsMarineForecastParser.parse(read("pomorci.xml"))
        assertTrue(forecast.regions.isNotEmpty())
    }

    @Test
    fun parsesSeaTemperature() {
        val sea = SeaTemperatureParser.parse(read("more_n.xml"))
        assertTrue(sea.stations.isNotEmpty())
    }

    @Test
    fun parsesUvIndex() {
        val uv = UvIndexParser.parse(read("uvi.xml"))
        assertTrue(uv.stations.isNotEmpty())
        assertTrue(uv.termHours.isNotEmpty())
    }

    @Test
    fun parsesBioForecast() {
        val bio = BioForecastParser.parse(read("bio_novo.xml"))
        assertTrue(bio.days.isNotEmpty())
    }

    @Test
    fun parsesFireDanger() {
        val fire = FireDangerParser.parse(read("indeks.xml"))
        assertTrue(fire.stations.isNotEmpty())
    }

    @Test
    fun parsesHeatAndColdWaves() {
        val heat = HeatColdWaveParser.parseHeat(read("toplinskival_5.xml"))
        val cold = HeatColdWaveParser.parseCold(read("hladnival.xml"))
        assertTrue(heat.cities.isNotEmpty())
        assertTrue(cold.cities.isNotEmpty())
    }

    @Test
    fun parsesEuropeObservations() {
        val europe = EuropeanWeatherParser.parse(read("europa_n.xml"))
        assertTrue(europe.cities.size > 5)
    }

    @Test
    fun parsesWaterTemperatures() {
        val snapshot = WaterTemperatureParser.parse(read("temp_vode.xml"))
        assertTrue(snapshot.series.isNotEmpty())
        val first = snapshot.series.first()
        assertTrue(first.stationCode.isNotEmpty())
        assertTrue(first.readings.isNotEmpty())
        assertNotNull(first.latest)
    }

    @Test
    fun parsesSnowDepths() {
        val snow = SnowDepthParser.parse(read("snijeg_n.xml"))
        assertTrue(snow.title.isNotEmpty())
        assertNotNull(snow.date)
        assertNotNull(snow.hour)
        // Summer fixture carries only the title — station list is empty.
        assertTrue(snow.stations.isEmpty())
    }

    @Test
    fun parsesYesterdayMeasurements() {
        val tn = DailyMeasurementParser.parseMinTemperature(read("tn.xml"))
        val tx = DailyMeasurementParser.parseMaxTemperature(read("tx.xml"))
        val pr = DailyMeasurementParser.parsePrecipitation(read("oborina.xml"))
        val t5 = DailyMeasurementParser.parseGroundTemperature(read("t5.xml"))
        assertTrue(tn.values.isNotEmpty())
        assertTrue(tx.values.isNotEmpty())
        assertTrue(pr.values.isNotEmpty())
        assertTrue(t5.values.isNotEmpty())
    }
}
