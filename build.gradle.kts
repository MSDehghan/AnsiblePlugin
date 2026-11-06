import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import javax.inject.Inject

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
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(providers.gradleProperty("javaVersion").get())
    }
}

abstract class InstallPluginDocs : DefaultTask() {
    @get:InputDirectory
    abstract val generatedDocs: DirectoryProperty

    @get:OutputDirectory
    abstract val docsDirectory: DirectoryProperty

    @get:OutputFile
    abstract val pluginsList: RegularFileProperty

    @get:Inject
    abstract val fileSystemOperations: FileSystemOperations

    @TaskAction
    fun install() {
        fileSystemOperations.sync {
            from(generatedDocs) { include("*.json") }
            into(docsDirectory)
        }
        generatedDocs.file("plugins_list.txt").get().asFile.copyTo(pluginsList.get().asFile, overwrite = true)
    }
}

tasks.register<InstallPluginDocs>("generatePluginDocs") {
    group = "documentation"
    description = "Generates and installs plugin documentation from the installed Ansible modules."
    dependsOn(":plugin-doc-extractor:generateModuleDocs")
    doNotTrackState("The sync must also remove files added outside Gradle.")
    generatedDocs = project(":plugin-doc-extractor").layout.buildDirectory.dir("generated/docs")
    docsDirectory = layout.projectDirectory.dir("src/main/resources/docs")
    pluginsList = layout.projectDirectory.file("src/main/resources/plugins_list.txt")
}

tasks.processResources {
    // Also support requesting generation and a plugin build in one invocation.
    mustRunAfter("generatePluginDocs")
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
