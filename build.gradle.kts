plugins {
    java
    jacoco
    id("org.sonarqube") version "3.0"
    id("org.jetbrains.intellij") version "0.5.0"
}

group = "ir.msdehghan"
version = "0.12-SNAPSHOT"

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
    updateSinceUntilBuild = false
}

sonarqube {
    properties {
        properties["sonar.projectKey"] = "MSDehghan_AnsiblePlugin"
        properties["sonar.organization"] = "msdehghan-github"
        properties["sonar.host.url"] = "https://sonarcloud.io"
    }
}

tasks{
    sonarqube.get().dependsOn(jacocoTestReport)
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.isEnabled = true
        }
    }
    buildSearchableOptions {
        enabled = false
    }
}