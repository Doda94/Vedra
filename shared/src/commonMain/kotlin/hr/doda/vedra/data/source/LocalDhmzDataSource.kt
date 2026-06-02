package hr.doda.vedra.data.source

import org.jetbrains.compose.resources.ExperimentalResourceApi
import vedra.shared.generated.resources.Res

/**
 * Loads a DHMZ XML fixture bundled in `composeResources/files/dhmz/` and
 * decodes it. Most files are UTF-8; `hladnival.xml` and `toplinskival_5.xml`
 * declare ISO-8859-1, but the bytes we ship are ASCII-compatible so UTF-8
 * decoding works for the data we currently bundle.
 */
@OptIn(ExperimentalResourceApi::class)
class LocalDhmzDataSource {

    suspend fun read(file: DhmzFile): String {
        val bytes = Res.readBytes("files/dhmz/${file.fileName}")
        return decode(bytes)
    }

    private fun decode(bytes: ByteArray): String {
        // Strip a UTF-8 BOM if present.
        val start = if (bytes.size >= 3 &&
            bytes[0] == 0xEF.toByte() &&
            bytes[1] == 0xBB.toByte() &&
            bytes[2] == 0xBF.toByte()
        ) 3 else 0
        return bytes.decodeToString(start, bytes.size)
    }
}

/** Catalogue of bundled DHMZ XML files. */
enum class DhmzFile(val fileName: String) {
    CURRENT_OBSERVATIONS("hrvatska_n.xml"),
    CURRENT_OBSERVATIONS_ALT("hrvatska1_n.xml"),
    SEVEN_DAY_FORECAST("7d_graf_i_simboli.xml"),
    FORECAST_TODAY("prognoza_danas.xml"),
    FORECAST_TOMORROW("prognoza_sutra.xml"),
    FORECAST_OUTLOOK("prognoza_izgledi.xml"),
    REGIONAL_DESCRIPTIONS("regije_danas.xml"),
    ALERTS_TODAY("cap_hr_today.xml"),
    ALERTS_TOMORROW("cap_hr_tomorrow.xml"),
    ALERTS_DAY_AFTER("cap_hr_day_after_tomorrow.xml"),
    HYDRO_BULLETIN("hidro_bilten.xml"),
    MARINE_FORECAST("jadran_h.xml"),
    MARINE_SAILORS("pomorci.xml"),
    SEA_TEMPERATURE("more_n.xml"),
    SEA_WATER_TEMPERATURE("temp_vode.xml"),
    UV_INDEX("uvi.xml"),
    BIO_FORECAST("bio_novo.xml"),
    FIRE_DANGER("indeks.xml"),
    HEAT_WAVE("toplinskival_5.xml"),
    COLD_WAVE("hladnival.xml"),
    EUROPE_OBSERVATIONS("europa_n.xml"),
    MIN_TEMPERATURE("tn.xml"),
    MAX_TEMPERATURE("tx.xml"),
    GROUND_TEMPERATURE("t5.xml"),
    PRECIPITATION("oborina.xml"),
}
