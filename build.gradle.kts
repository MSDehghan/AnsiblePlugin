import org.jetbrains.intellij.platform.gradle.TestFrameworkType

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
        intellijIdeaCommunity("2024.3") // TODO: after finding the problem with 243 version we should set to 242
        bundledPlugins("org.jetbrains.plugins.yaml")
        testFramework(TestFrameworkType.Platform)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

intellijPlatform {
    pluginConfiguration {
        intellijPlatform {
            buildSearchableOptions = false
        }

        ideaVersion {
            sinceBuild = "243"
            untilBuild = ""
        }
    }
}


sonarqube {
    properties {
        properties["sonar.projectKey"] = "MSDehghan_AnsiblePlugin"
        properties["sonar.organization"] = "msdehghan-github"
        properties["sonar.host.url"] = "https://sonarcloud.io"
    }
}

tasks {
    sonarqube.get().dependsOn(jacocoTestReport)
    jacocoTestReport {
        dependsOn(test)
        reports {
            xml.required.set(true)
        }
    }
}