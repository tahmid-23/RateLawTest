plugins {
    kotlin("jvm") version "1.7.0"
    id("org.openjfx.javafxplugin") version "0.0.13"
}

group = "com.github.tahmid_23"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.letsPlot.api)
    implementation(libs.letsPlot.jfx)
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

javafx {
    version = "17.0.2"
    modules = listOf("javafx.swing")
}