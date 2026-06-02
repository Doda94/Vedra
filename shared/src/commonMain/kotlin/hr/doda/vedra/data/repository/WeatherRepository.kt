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
import hr.doda.vedra.data.parser.UvIndexParser
import hr.doda.vedra.data.source.DhmzFile
import hr.doda.vedra.data.source.LocalDhmzDataSource
import hr.doda.vedra.domain.alert.HydroBulletin
import hr.doda.vedra.domain.alert.MeteoAlertSet
import hr.doda.vedra.domain.europe.EuropeanWeatherSnapshot
import hr.doda.vedra.domain.forecast.CityForecast
import hr.doda.vedra.domain.forecast.NationalDailyForecast
import hr.doda.vedra.domain.forecast.OutlookForecast
import hr.doda.vedra.domain.forecast.RegionalDescriptions
import hr.doda.vedra.domain.indices.BioForecast
import hr.doda.vedra.domain.indices.FireDangerSnapshot
import hr.doda.vedra.domain.indices.HeatColdWaveSnapshot
import hr.doda.vedra.domain.indices.UvIndexSnapshot
import hr.doda.vedra.domain.observation.CurrentObservationSnapshot
import hr.doda.vedra.domain.observation.DailyStationMeasurements
import hr.doda.vedra.domain.sea.MarineForecast
import hr.doda.vedra.domain.sea.SailorsMarineForecast
import hr.doda.vedra.domain.sea.SeaTemperatureSnapshot

/**
 * High-level access to all DHMZ data. Each function reads a file via the
 * [LocalDhmzDataSource] and returns parsed domain objects.
 *
 * When networking is added later, swap the data source for a remote one
 * (parsers are agnostic — they take a String).
 */
class WeatherRepository(
    private val source: LocalDhmzDataSource = LocalDhmzDataSource(),
) {

    suspend fun currentObservations(): CurrentObservationSnapshot =
        CurrentObservationsParser.parse(source.read(DhmzFile.CURRENT_OBSERVATIONS))

    suspend fun sevenDayForecast(): List<CityForecast> =
        SevenDayForecastParser.parse(source.read(DhmzFile.SEVEN_DAY_FORECAST))

    suspend fun sevenDayForecastFor(cityNameOrCode: String): CityForecast? =
        SevenDayForecastParser.parseCity(
            xml = source.read(DhmzFile.SEVEN_DAY_FORECAST),
            cityNameOrCode = cityNameOrCode,
        )

    suspend fun forecastToday(): NationalDailyForecast =
        NationalDailyForecastParser.parse(source.read(DhmzFile.FORECAST_TODAY))

    suspend fun forecastTomorrow(): NationalDailyForecast =
        NationalDailyForecastParser.parse(source.read(DhmzFile.FORECAST_TOMORROW))

    suspend fun outlookForecast(): OutlookForecast =
        OutlookForecastParser.parse(source.read(DhmzFile.FORECAST_OUTLOOK))

    suspend fun regionalDescriptions(): RegionalDescriptions =
        RegionalDescriptionsParser.parse(source.read(DhmzFile.REGIONAL_DESCRIPTIONS))

    suspend fun alerts(target: MeteoAlertSet.Target = MeteoAlertSet.Target.TODAY): MeteoAlertSet {
        val file = when (target) {
            MeteoAlertSet.Target.TODAY -> DhmzFile.ALERTS_TODAY
            MeteoAlertSet.Target.TOMORROW -> DhmzFile.ALERTS_TOMORROW
            MeteoAlertSet.Target.DAY_AFTER_TOMORROW -> DhmzFile.ALERTS_DAY_AFTER
        }
        return MeteoAlertParser.parse(source.read(file), target)
    }

    suspend fun hydroBulletin(): HydroBulletin =
        HydroBulletinParser.parse(source.read(DhmzFile.HYDRO_BULLETIN))

    suspend fun marineForecast(): MarineForecast =
        MarineForecastParser.parse(source.read(DhmzFile.MARINE_FORECAST))

    suspend fun sailorsMarineForecast(): SailorsMarineForecast =
        SailorsMarineForecastParser.parse(source.read(DhmzFile.MARINE_SAILORS))

    suspend fun seaTemperature(): SeaTemperatureSnapshot =
        SeaTemperatureParser.parse(source.read(DhmzFile.SEA_TEMPERATURE))

    suspend fun uvIndex(): UvIndexSnapshot =
        UvIndexParser.parse(source.read(DhmzFile.UV_INDEX))

    suspend fun bioForecast(): BioForecast =
        BioForecastParser.parse(source.read(DhmzFile.BIO_FORECAST))

    suspend fun fireDanger(): FireDangerSnapshot =
        FireDangerParser.parse(source.read(DhmzFile.FIRE_DANGER))

    suspend fun heatWave(): HeatColdWaveSnapshot =
        HeatColdWaveParser.parseHeat(source.read(DhmzFile.HEAT_WAVE))

    suspend fun coldWave(): HeatColdWaveSnapshot =
        HeatColdWaveParser.parseCold(source.read(DhmzFile.COLD_WAVE))

    suspend fun europeanWeather(): EuropeanWeatherSnapshot =
        EuropeanWeatherParser.parse(source.read(DhmzFile.EUROPE_OBSERVATIONS))

    suspend fun yesterdayMinTemperatures(): DailyStationMeasurements =
        DailyMeasurementParser.parseMinTemperature(source.read(DhmzFile.MIN_TEMPERATURE))

    suspend fun yesterdayMaxTemperatures(): DailyStationMeasurements =
        DailyMeasurementParser.parseMaxTemperature(source.read(DhmzFile.MAX_TEMPERATURE))

    suspend fun yesterdayPrecipitation(): DailyStationMeasurements =
        DailyMeasurementParser.parsePrecipitation(source.read(DhmzFile.PRECIPITATION))

    suspend fun groundTemperatures(): DailyStationMeasurements =
        DailyMeasurementParser.parseGroundTemperature(source.read(DhmzFile.GROUND_TEMPERATURE))
}
