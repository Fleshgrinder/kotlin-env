plugins {
    `java-library`
    `maven-publish`
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-annotations-jvm:$embeddedKotlinVersion")
    compileOnly("org.jetbrains:annotations:23.0.0")
}

java {
    toolchain.languageVersion.set(libs.versions.java.map(JavaLanguageVersion::of))
}
