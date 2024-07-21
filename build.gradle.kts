import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    id("idea")
    kotlin("jvm") version "1.9.10"
}

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "idea")
    apply(plugin = "kotlin")

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
}

subprojects {
    dependencies {
        testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
        testImplementation("io.kotest:kotest-assertions-core:5.9.1")
        testImplementation("io.kotest:kotest-framework-engine:5.9.1")
        testImplementation("io.mockk:mockk:1.13.12")
    }

    tasks.named<Test>("test") {
        useJUnitPlatform()
    }
}
