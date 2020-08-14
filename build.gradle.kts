plugins {
    java
    id("org.jetbrains.intellij") version "0.4.21"
}

group = "ir.msdehghan"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.2")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

intellij {
    version = "2020.1.4"
    setPlugins("yaml")
}

tasks.buildSearchableOptions {
    enabled = false
}