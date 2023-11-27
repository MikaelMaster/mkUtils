plugins {
    java
    kotlin("jvm") version "1.9.21"
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
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://jitpack.io/")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.17.1-R0.1-SNAPSHOT")
    compileOnly("net.md-5:bungeecord-api:1.16-R0.3")
    compileOnly(files("libs/spigot-1.17.1.jar"))
    api(kotlin("stdlib"))
    api(kotlin("reflect"))
    api("redis.clients:jedis:4.3.1")
    api("org.slf4j:slf4j-api:2.0.5")
    api("org.slf4j:slf4j-log4j12:2.0.5")
    api(files("C:\\Users\\Usuario\\Desktop\\IntelliJ Global Depends\\EduardAPI-1.0-all.jar\\"))
}

tasks {
    jar {
        destinationDirectory
            .set(file("C:\\Users\\Usuario\\Desktop\\Meus Plugins - Jars\\"))
    }
    compileJava {
        options.encoding = "UTF-8"
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    shadowJar {
        archiveVersion.set("2.0")
        archiveBaseName.set("mkUtils")
        destinationDirectory.set(
            file("C:\\Users\\Usuario\\Desktop\\Meus Plugins - Jars\\")
        )
    }
}
publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}