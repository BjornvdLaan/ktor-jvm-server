plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.kover)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.sqldelight)
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.fortysevendegrees"
version = "0.1"

repositories {
    mavenCentral()
}

sqldelight {
    databases {
        create("NativePostgres") {
            packageName.set("com.fortysevendegrees.sqldelight")
            dialect("app.cash.sqldelight:postgresql-dialect:2.0.0-alpha05")
        }
    }
    //linkSqlite.set(false)
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.fortysevendegrees.MainKt"
    }
}

kotlin {

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "19"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.arrow.fx)
                implementation(libs.suspendapp)
                implementation(libs.suspendapp.ktor)
                implementation(libs.bundles.ktor.server)
                implementation("com.zaxxer:HikariCP:5.0.1")
                implementation("app.cash.sqldelight:jdbc-driver:2.0.0-alpha05")
                implementation("org.postgresql:postgresql:42.2.20")
                runtimeOnly("org.postgresql:postgresql")
            }
        }
    }
}
