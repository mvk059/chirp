package fyi.manpreet.chirp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class ChirpBackendApplication

fun main(args: Array<String>) {
    runApplication<ChirpBackendApplication>(*args)
}
