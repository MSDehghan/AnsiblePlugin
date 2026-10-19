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


// See README.md in this subproject for generation and installation instructions.
