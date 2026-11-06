plugins {
    application
}

application {
    mainClass = "ir.msdehghan.plugins.ansible.extractor.ExtractDoc"
}

repositories{
    mavenCentral()
}

dependencies {
    implementation(libs.jackson.databind)
    testImplementation(libs.junit)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(providers.gradleProperty("javaVersion").get())
    }
}

val collectedDocs = layout.buildDirectory.file("ansible-doc/plugins.json")

// Declares no outputs, so it always reruns and picks up collection installs and removals.
val collectModuleDocs = tasks.register<Exec>("collectModuleDocs") {
    description = "Collects documentation for all installed Ansible modules."
    commandLine(
        "python3",
        layout.projectDirectory.file("collect_module_docs.py").asFile.absolutePath,
        collectedDocs.get().asFile.absolutePath,
    )
}

tasks.register<JavaExec>("generateModuleDocs") {
    description = "Converts collected Ansible documentation into plugin JSON files."
    dependsOn(collectModuleDocs)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass = application.mainClass
    val output = layout.buildDirectory.dir("generated/docs")
    inputs.file(collectedDocs)
    outputs.dir(output)
    // Start from an empty directory so removed modules don't linger.
    doFirst { output.get().asFile.deleteRecursively() }
    args("--input", collectedDocs.get().asFile.absolutePath, "--output", output.get().asFile.absolutePath)
}
