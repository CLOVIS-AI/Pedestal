plugins {
    kotlin("multiplatform") apply false
    id("com.palantir.git-version")
}

group = "opensavvy"
version = calculateVersion()

subprojects {
    group = rootProject.group
    version = rootProject.version
}

allprojects {
    repositories {
        mavenCentral()
    }
}

fun calculateVersion(): String {
    val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
    val details = versionDetails()

    return if (details.commitDistance == 0)
        details.lastTag
    else
        "${details.lastTag}-post.${details.commitDistance}+${details.gitHash}"
}
