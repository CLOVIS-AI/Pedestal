plugins {
    kotlin("multiplatform") version "1.7.0" apply false
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
