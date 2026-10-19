import org.jetbrains.intellij.platform.gradle.TestFrameworkType

plugins {
    java
    alias(libs.plugins.intelliJPlatform)
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
    testImplementation(project(":plugin-doc-extractor"))
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
