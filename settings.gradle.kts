plugins {
    id("com.fleshgrinder.git") version "unspecified"
    id("com.fleshgrinder.github-release") version "unspecified"
}

rootProject.name = "jvm-env"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include("examples", "java", "kotlin")
