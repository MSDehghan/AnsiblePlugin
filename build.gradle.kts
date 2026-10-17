import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    java
    jacoco
    alias(libs.plugins.intelliJPlatform)
    alias(libs.plugins.sonarqube)
}

group = "ir.msdehghan"
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    // The IDE's own Jackson lives in internal platform modules that plugins can't depend on, so bundle it.
    implementation(libs.jackson.databind)
    testImplementation(libs.junit)
    testImplementation(libs.opentest4j)
    intellijPlatform {
        intellijIdea(providers.gradleProperty("platformVersion"))
        bundledPlugins("org.jetbrains.plugins.yaml", "com.intellij.modules.json")
        testFramework(TestFrameworkType.Platform)
        // Workaround for https://github.com/JetBrains/intellij-platform-gradle-plugin/issues/2261
        testBundledPlugins("com.intellij.modules.ultimate")
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(providers.gradleProperty("javaVersion").get())
    }
}

intellijPlatform {
    buildSearchableOptions = false

    pluginConfiguration {
        version = providers.gradleProperty("pluginVersion")
        ideaVersion {
            sinceBuild = providers.gradleProperty("pluginSinceBuild")
        }
    }

    pluginVerification {
        ides {
            // Locally verify only against the IDE we build with; CI checks the recommended set.
            if (providers.environmentVariable("CI").isPresent) recommended() else current()
        }
        // The unified 2026.x layout lists some jars that don't exist on disk; don't warn about each one.
        freeArgs = listOf("-missing-layout-classpath-file", "skip-silently")
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
    named("sonar") {
        dependsOn(jacocoTestReport)
    }
}
