package fyi.manpreet.chirp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform