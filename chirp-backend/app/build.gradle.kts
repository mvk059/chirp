plugins {
    id("chirp.spring-boot-app")
}

group = "fyi.manpreet"
version = "0.0.1-SNAPSHOT"
description = "chirp-backend"

dependencies {
    implementation(projects.user)
    implementation(projects.chat)
    implementation(projects.notification)
    implementation(projects.common)

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql)
}