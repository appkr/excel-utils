import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("idea")
    kotlin("jvm") version "1.9.10"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1"
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "idea")
    apply(plugin = "kotlin")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    group = "dev.appkr"
    version = "0.0.1-SNAPSHOT"

    java {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            jvmTarget = "17"
        }
    }

    repositories {
        mavenCentral()
    }

    ktlint {
        version.set("0.49.1")
        android.set(false)
        outputToConsole.set(true)
        coloredOutput.set(true)
        reporters {
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
            reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
        }
    }
}

subprojects {
    dependencies {
        testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
        testImplementation("io.kotest:kotest-assertions-core:5.9.1")
        testImplementation("io.kotest:kotest-framework-engine:5.9.1")
        testImplementation("io.kotest:kotest-framework-datatest:5.9.1")
        testImplementation("io.mockk:mockk:1.13.12")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}
