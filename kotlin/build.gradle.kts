plugins {
    kotlin("jvm") version embeddedKotlinVersion
    `maven-publish`
}

java {
    toolchain.languageVersion.set(libs.versions.java.map(JavaLanguageVersion::of))
}

kotlin {
    explicitApi()
}
