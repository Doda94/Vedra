package hr.doda.vedra.domain.alert

import kotlinx.datetime.Instant

/**
 * Severity level as defined by the EUMETNET / Meteoalarm scale.
 * Severity→color mapping is a UI concern — see the Compose theme (phase 7),
 * which must pair color with icon + label for color-blind accessibility.
 */
enum class AlertSeverity(val level: Int) {
    GREEN(1),
    YELLOW(2),
    ORANGE(3),
    RED(4),
    UNKNOWN(0);

    companion object {
        fun fromAwarenessLevel(text: String?): AlertSeverity {
            if (text == null) return UNKNOWN
            // Format observed: "2; yellow; Moderate"
            val n = text.trim().substringBefore(';').toIntOrNull() ?: return UNKNOWN
            return entries.firstOrNull { it.level == n } ?: UNKNOWN
        }

        fun fromName(name: String?): AlertSeverity = when (name?.lowercase()) {
            "minor", "green" -> GREEN
            "moderate", "yellow" -> YELLOW
            "severe", "orange" -> ORANGE
            "extreme", "red" -> RED
            else -> UNKNOWN
        }
    }
}

/** Type of weather hazard. */
enum class AlertType(val code: Int, val displayName: String) {
    WIND(1, "Vjetar"),
    SNOW_ICE(2, "Snijeg / led"),
    THUNDERSTORM(3, "Grmljavinska oluja"),
    FOG(4, "Magla"),
    HIGH_TEMPERATURE(5, "Visoke temperature"),
    LOW_TEMPERATURE(6, "Niske temperature"),
    COASTAL_EVENT(7, "Obalna pojava"),
    FOREST_FIRE(8, "Šumski požar"),
    AVALANCHES(9, "Lavine"),
    RAIN(10, "Kiša"),
    FLOOD(12, "Poplava"),
    RAIN_FLOOD(11, "Kiša / poplava"),
    UNKNOWN(0, "Nepoznato");

    companion object {
        fun fromAwarenessType(text: String?): AlertType {
            if (text == null) return UNKNOWN
            // Format observed: "3; Thunderstorm"
            val n = text.trim().substringBefore(';').toIntOrNull() ?: return UNKNOWN
            return entries.firstOrNull { it.code == n } ?: UNKNOWN
        }
    }
}

/** Localised text payload of a CAP `<info>` block. */
data class MeteoAlertInfo(
    val language: String,
    val event: String,
    val severity: AlertSeverity,
    val type: AlertType,
    val onset: Instant?,
    val expires: Instant?,
    val description: String,
    val instruction: String,
    val areas: List<String>,
    /** EMMA region codes affected (e.g. "HR006"). */
    val regionCodes: List<String>,
)

/** A full CAP 1.2 alert (one file => one alert with multiple `<info>` blocks). */
data class MeteoAlert(
    val identifier: String,
    val sender: String,
    val sent: Instant?,
    val msgType: String,
    val infos: List<MeteoAlertInfo>,
) {
    fun localized(language: String): MeteoAlertInfo? =
        infos.firstOrNull { it.language.equals(language, ignoreCase = true) }
}

/** All alerts for a given target day. */
data class MeteoAlertSet(
    val target: Target,
    val alerts: List<MeteoAlert>,
) {
    enum class Target { TODAY, TOMORROW, DAY_AFTER_TOMORROW }
}

/** Hydrological bulletin (`hidro_bilten.xml`). */
data class HydroBulletin(
    val period: String,
    val recordedAt: String,
    val warning: String,
    val rivers: Map<String, String>,
)
