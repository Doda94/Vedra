package hr.doda.vedra.data.source

/**
 * Abstraction over where DHMZ XML files come from. [LocalDhmzDataSource]
 * reads bundled fixtures; a `RemoteDhmzDataSource` (phase 8) will fetch
 * the same files from vrijeme.hr.
 *
 * Implementations return the raw XML as a decoded [String]; parsing is
 * the caller's job (see `data/parser`).
 */
interface DhmzDataSource {
    suspend fun read(file: DhmzFile): String
}
