package hr.doda.vedra.domain.city

/** Geographic coordinates. */
data class Coordinates(val latitude: Double, val longitude: Double)

/** A weather station / city as identified by DHMZ. */
data class City(
    val name: String,
    val code: String? = null,
    val coordinates: Coordinates? = null,
    /** Whether this is an automatic (`autom="1"`) station. */
    val automatic: Boolean = false,
)

/**
 * Croatian forecast macro-regions used by DHMZ in `prognoza_*` and
 * `regije_danas` files.
 */
enum class Region(val key: String, val displayName: String) {
    CENTRAL("sredisnja", "Središnja Hrvatska"),
    EASTERN("istocna", "Istočna Hrvatska"),
    MOUNTAINOUS("gorska", "Gorska Hrvatska"),
    INLAND_DALMATIA("unutrasnjost Dalmacije", "Unutrašnjost Dalmacije"),
    NORTHERN_ADRIATIC("sjeverni Jadran", "Sjeverni Jadran"),
    CENTRAL_ADRIATIC("srednji Jadran", "Srednji Jadran"),
    SOUTHERN_ADRIATIC("juzni Jadran", "Južni Jadran"),
    ISTRIA("istra", "Istra"),
    DALMATIA("dalmacija", "Dalmacija");

    companion object {
        fun fromKey(key: String): Region? =
            entries.firstOrNull { it.key.equals(key, ignoreCase = true) }
    }
}
