plugins {
    id("java-library")
    id("chirp.spring-boot-service")
    kotlin("plugin.jpa")
}

group = "fyi.manpreet"
version = "unspecified"

repositories {
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
}

dependencies {
    implementation(projects.common)

    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.validation)
    implementation(libs.spring.boot.starter.amqp)
    implementation(libs.spring.boot.starter.websocket)
    runtimeOnly(libs.postgresql)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}