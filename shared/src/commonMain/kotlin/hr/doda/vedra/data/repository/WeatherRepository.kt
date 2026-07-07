package hr.doda.vedra.data.repository

import hr.doda.vedra.data.parser.BioForecastParser
import hr.doda.vedra.data.parser.CurrentObservationsParser
import hr.doda.vedra.data.parser.DailyMeasurementParser
import hr.doda.vedra.data.parser.EuropeanWeatherParser
import hr.doda.vedra.data.parser.FireDangerParser
import hr.doda.vedra.data.parser.HeatColdWaveParser
import hr.doda.vedra.data.parser.HydroBulletinParser
import hr.doda.vedra.data.parser.MarineForecastParser
import hr.doda.vedra.data.parser.MeteoAlertParser
import hr.doda.vedra.data.parser.NationalDailyForecastParser
import hr.doda.vedra.data.parser.OutlookForecastParser
import hr.doda.vedra.data.parser.RegionalDescriptionsParser
import hr.doda.vedra.data.parser.SailorsMarineForecastParser
import hr.doda.vedra.data.parser.SeaTemperatureParser
import hr.doda.vedra.data.parser.SevenDayForecastParser
import hr.doda.vedra.data.parser.SnowDepthParser
import hr.doda.vedra.data.parser.UvIndexParser
import hr.doda.vedra.data.parser.WaterTemperatureParser
import hr.doda.vedra.data.source.DhmzDataSource
import hr.doda.vedra.data.source.DhmzFile
import hr.doda.vedra.domain.alert.HydroBulletin
import hr.doda.vedra.domain.alert.MeteoAlertSet
import hr.doda.vedra.domain.europe.EuropeanWeatherSnapshot
import hr.doda.vedra.domain.forecast.CityForecast
import hr.doda.vedra.domain.forecast.NationalDailyForecast
import hr.doda.vedra.domain.forecast.OutlookForecast
import hr.doda.vedra.domain.forecast.RegionalDescriptions
import hr.doda.vedra.domain.hydro.WaterTemperatureSnapshot
import hr.doda.vedra.domain.indices.BioForecast
import hr.doda.vedra.domain.indices.FireDangerSnapshot
import hr.doda.vedra.domain.indices.HeatColdWaveSnapshot
import hr.doda.vedra.domain.indices.UvIndexSnapshot
import hr.doda.vedra.domain.observation.CurrentObservationSnapshot
import hr.doda.vedra.domain.observation.DailyStationMeasurements
import hr.doda.vedra.domain.observation.SnowDepthSnapshot
import hr.doda.vedra.domain.sea.MarineForecast
import hr.doda.vedra.domain.sea.SailorsMarineForecast
import hr.doda.vedra.domain.sea.SeaTemperatureSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

/**
 * High-level access to all DHMZ data. Each function reads a file via the
 * injected [DhmzDataSource] and returns parsed domain objects.
 *
 * Main-safe: reading and parsing run on [Dispatchers.Default]. Results
 * are cached in memory per file, so repeated calls (e.g. from several
 * ViewModels) parse each file at most once. Call [invalidate] to force a
 * re-read — pull-to-refresh will use this; phase 8 adds time-based
 * expiration on top of [CacheEntry.parsedAtMs].
 */
class WeatherRepository(
    private val source: DhmzDataSource,
) {
    private class CacheEntry(
        val value: Any,
        val parsedAtMs: Long,
    )

    private val mapLock = Mutex()
    private val fileLocks = mutableMapOf<DhmzFile, Mutex>()
    private val cache = mutableMapOf<DhmzFile, CacheEntry>()

    /** Drops all cached results; the next call per file re-reads and re-parses. */
    suspend fun invalidate() {
        mapLock.withLock { cache.clear() }
    }

    private suspend fun <T : Any> cached(
        file: DhmzFile,
        parse: (String) -> T,
    ): T {
        val fileLock = mapLock.withLock { fileLocks.getOrPut(file) { Mutex() } }
        return fileLock.withLock {
            val hit = mapLock.withLock { cache[file] }
            if (hit != null) {
                @Suppress("UNCHECKED_CAST")
                hit.value as T
            } else {
                val parsed =
                    withContext(Dispatchers.Default) {
                        parse(source.read(file))
                    }
                mapLock.withLock {
                    cache[file] = CacheEntry(parsed, Clock.System.now().toEpochMilliseconds())
                }
                parsed
            }
        }
    }

    suspend fun currentObservations(): CurrentObservationSnapshot =
        cached(DhmzFile.CURRENT_OBSERVATIONS) { CurrentObservationsParser.parse(it) }

    suspend fun sevenDayForecast(): List<CityForecast> = cached(DhmzFile.SEVEN_DAY_FORECAST) { SevenDayForecastParser.parse(it) }

    suspend fun sevenDayForecastFor(cityNameOrCode: String): CityForecast? =
        sevenDayForecast().firstOrNull {
            it.cityName.equals(cityNameOrCode, ignoreCase = true) ||
                it.cityCode.equals(cityNameOrCode, ignoreCase = true)
        }

    suspend fun forecastToday(): NationalDailyForecast = cached(DhmzFile.FORECAST_TODAY) { NationalDailyForecastParser.parse(it) }

    suspend fun forecastTomorrow(): NationalDailyForecast = cached(DhmzFile.FORECAST_TOMORROW) { NationalDailyForecastParser.parse(it) }

    suspend fun outlookForecast(): OutlookForecast = cached(DhmzFile.FORECAST_OUTLOOK) { OutlookForecastParser.parse(it) }

    suspend fun regionalDescriptions(): RegionalDescriptions =
        cached(DhmzFile.REGIONAL_DESCRIPTIONS) { RegionalDescriptionsParser.parse(it) }

    suspend fun alerts(target: MeteoAlertSet.Target = MeteoAlertSet.Target.TODAY): MeteoAlertSet {
        val file =
            when (target) {
                MeteoAlertSet.Target.TODAY -> DhmzFile.ALERTS_TODAY
                MeteoAlertSet.Target.TOMORROW -> DhmzFile.ALERTS_TOMORROW
                MeteoAlertSet.Target.DAY_AFTER_TOMORROW -> DhmzFile.ALERTS_DAY_AFTER
            }
        return cached(file) { MeteoAlertParser.parse(it, target) }
    }

    suspend fun hydroBulletin(): HydroBulletin = cached(DhmzFile.HYDRO_BULLETIN) { HydroBulletinParser.parse(it) }

    suspend fun marineForecast(): MarineForecast = cached(DhmzFile.MARINE_FORECAST) { MarineForecastParser.parse(it) }

    suspend fun sailorsMarineForecast(): SailorsMarineForecast = cached(DhmzFile.MARINE_SAILORS) { SailorsMarineForecastParser.parse(it) }

    suspend fun seaTemperature(): SeaTemperatureSnapshot = cached(DhmzFile.SEA_TEMPERATURE) { SeaTemperatureParser.parse(it) }

    suspend fun waterTemperatures(): WaterTemperatureSnapshot = cached(DhmzFile.WATER_TEMPERATURE) { WaterTemperatureParser.parse(it) }

    suspend fun snowDepths(): SnowDepthSnapshot = cached(DhmzFile.SNOW_DEPTH) { SnowDepthParser.parse(it) }

    suspend fun uvIndex(): UvIndexSnapshot = cached(DhmzFile.UV_INDEX) { UvIndexParser.parse(it) }

    suspend fun bioForecast(): BioForecast = cached(DhmzFile.BIO_FORECAST) { BioForecastParser.parse(it) }

    suspend fun fireDanger(): FireDangerSnapshot = cached(DhmzFile.FIRE_DANGER) { FireDangerParser.parse(it) }

    suspend fun heatWave(): HeatColdWaveSnapshot = cached(DhmzFile.HEAT_WAVE) { HeatColdWaveParser.parseHeat(it) }

    suspend fun coldWave(): HeatColdWaveSnapshot = cached(DhmzFile.COLD_WAVE) { HeatColdWaveParser.parseCold(it) }

    suspend fun europeanWeather(): EuropeanWeatherSnapshot = cached(DhmzFile.EUROPE_OBSERVATIONS) { EuropeanWeatherParser.parse(it) }

    suspend fun yesterdayMinTemperatures(): DailyStationMeasurements =
        cached(DhmzFile.MIN_TEMPERATURE) { DailyMeasurementParser.parseMinTemperature(it) }

    suspend fun yesterdayMaxTemperatures(): DailyStationMeasurements =
        cached(DhmzFile.MAX_TEMPERATURE) { DailyMeasurementParser.parseMaxTemperature(it) }

    suspend fun yesterdayPrecipitation(): DailyStationMeasurements =
        cached(DhmzFile.PRECIPITATION) { DailyMeasurementParser.parsePrecipitation(it) }

    suspend fun groundTemperatures(): DailyStationMeasurements =
        cached(DhmzFile.GROUND_TEMPERATURE) { DailyMeasurementParser.parseGroundTemperature(it) }
}
