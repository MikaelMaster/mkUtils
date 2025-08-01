import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    java
    kotlin("jvm") version "2.2.0"
    `maven-publish`
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

group = "com.mikael"
version = "2.0"

java.sourceCompatibility = JavaVersion.VERSION_17
java.targetCompatibility = JavaVersion.VERSION_17

repositories {
    mavenLocal()
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://jitpack.io/")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    // Kotlin Standard Library and Reflection
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    // Jedis for the RedisAPI
    implementation("redis.clients:jedis:4.3.1")

    // SLF4J for logging
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("org.slf4j:slf4j-log4j12:2.0.5")

    // EduardAPI - legacy utilities for Minecraft plugins
    implementation(files("C:\\Users\\Usuario\\Desktop\\IntelliJ Global Depends\\EduardAPI-1.0-all.jar"))

    // PaperMC and BungeeCord APIs
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.3")
}

tasks {
    jar {
        destinationDirectory.set(file("C:\\Users\\Usuario\\Desktop\\Meus Plugins - Jars"))
    }
    shadowJar {
        archiveVersion.set("2.0")
        archiveBaseName.set("mkUtils")
        destinationDirectory.set(
            file("C:\\Users\\Usuario\\Desktop\\Meus Plugins - Jars")
        )
    }
    compileJava {
        options.encoding = "UTF-8"
    }
    compileKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
    compileTestKotlin {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_17)
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}