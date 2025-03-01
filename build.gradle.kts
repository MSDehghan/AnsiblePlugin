import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease

plugins {
    java
    jacoco
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.sonarqube)
}

group = "ir.msdehghan"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    testImplementation(libs.junit)
    intellijPlatform {
        intellijIdeaCommunity("2024.3")
        bundledPlugins("org.jetbrains.plugins.yaml", "com.intellij.modules.json")
        testFramework(TestFrameworkType.Platform)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "243"
        }

        intellijPlatform {
            buildSearchableOptions = false
            pluginVerification {
                ides {
                    recommended()
                }
            }
        }
    }
}

sonar {
    properties {
        property("sonar.projectKey", "MSDehghan_AnsiblePlugin")
        property("sonar.organization", "msdehghan")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

tasks {
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
        }
    }
    sonar.get().dependsOn(jacocoTestReport)
}