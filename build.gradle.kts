plugins {
    kotlin("jvm") version "1.9.23"
    id("org.jetbrains.dokka") version "1.8.20"
    id("org.jetbrains.kotlinx.kover") version "0.7.5"
    id("com.adarshr.test-logger") version "4.0.0"}

group = "be.vamaralds"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    // Functional Programming
    implementation("io.arrow-kt:arrow-core:1.2.0")
    implementation("io.arrow-kt:arrow-fx-coroutines:1.2.0")

    // JSON Serialization
    implementation("org.json:json:20240303")

    // Testing
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.8.10")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")}

tasks.test {
    useJUnitPlatform()

    testlogger {
        setTheme("standard")
        showExceptions = true
        showStackTraces = true
        showFullStackTraces = false
        showCauses = true
        slowThreshold = 2000
        showSummary = true
        showSimpleNames = false
        showPassed = true
        showSkipped = true
        showFailed = true
        showOnlySlow = false
        showStandardStreams = false
        showPassedStandardStreams = true
        showSkippedStandardStreams = true
        showFailedStandardStreams = true
        setLogLevel("lifecycle")
    }
}

kotlin {
    jvmToolchain(21)
}

tasks.dokkaHtml {
    outputDirectory.set(rootDir.resolve("docs/"))
}

koverReport {
    filters {
        excludes {
            classes("be.vamaralds.padomi.mxp.metamodel.*", "be.vamaralds.padomi.config.*")
        }
    }

    verify {
        rule {
            isEnabled = true
        }
    }
}