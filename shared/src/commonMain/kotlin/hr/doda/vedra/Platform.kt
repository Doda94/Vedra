package hr.doda.vedra

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform