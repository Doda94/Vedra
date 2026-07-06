package hr.doda.vedra.domain.observation

import kotlinx.datetime.LocalDate

/** Snow depth at one station (`snijeg_n.xml`). */
data class SnowDepth(
    val stationName: String,
    val depthCm: Double?,
)

/**
 * Snow depths across Croatia from `snijeg_n.xml`.
 *
 * Outside the snow season the file carries only the `<naslov>` title and
 * [stations] is empty — design the UI for that case. The per-station
 * format has only been observed indirectly (no winter fixture yet); see
 * `SnowDepthParser` for the tags it tries.
 */
data class SnowDepthSnapshot(
    val title: String,
    val date: LocalDate?,
    val hour: Int?,
    val stations: List<SnowDepth>,
)
