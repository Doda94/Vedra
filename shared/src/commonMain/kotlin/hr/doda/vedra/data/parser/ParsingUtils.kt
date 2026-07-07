package hr.doda.vedra.data.parser

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/** Helpers shared across DHMZ XML parsers. */
internal object ParsingUtils {
    /**
     * DHMZ frequently uses `-`, `/`, empty strings or whitespace to
     * indicate a missing measurement. Returns null in those cases.
     */
    fun cleanText(raw: String?): String? {
        if (raw == null) return null
        val t = raw.trim()
        if (t.isEmpty() || t == "-" || t == "/" || t == "—") return null
        return t
    }

    fun parseDouble(raw: String?): Double? = cleanText(raw)?.replace(',', '.')?.toDoubleOrNull()

    fun parseInt(raw: String?): Int? = cleanText(raw)?.toIntOrNull()

    /** Parses dd.MM.yyyy (with or without trailing dot). */
    fun parseDmyDate(raw: String?): LocalDate? {
        val t = cleanText(raw)?.removeSuffix(".") ?: return null
        val parts = t.split('.').mapNotNull { it.trim().toIntOrNull() }
        if (parts.size < 3) return null
        return runCatching { LocalDate(parts[2], parts[1], parts[0]) }.getOrNull()
    }

    /** Parses ddMMyy. */
    fun parseDdmmyyDate(raw: String?): LocalDate? {
        val t = cleanText(raw) ?: return null
        if (t.length != 6) return null
        val day = t.substring(0, 2).toIntOrNull() ?: return null
        val month = t.substring(2, 4).toIntOrNull() ?: return null
        val year = 2000 + (t.substring(4, 6).toIntOrNull() ?: return null)
        return runCatching { LocalDate(year, month, day) }.getOrNull()
    }

    /** Parses dd.MM.yyyy hh:mm style timestamps where the time is appended. */
    fun parseDateAtHour(
        date: LocalDate,
        hour: Int,
    ): LocalDateTime = LocalDateTime(date.year, date.monthNumber, date.dayOfMonth, hour.coerceIn(0, 23), 0)
}
