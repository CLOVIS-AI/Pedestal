plugins {
    kotlin("multiplatform") apply false
}

group = "opensavvy"
version = "1.0-SNAPSHOT"

subprojects {
    group = rootProject.group
    version = rootProject.version
}

allprojects {
    repositories {
        mavenCentral()
    }
}
