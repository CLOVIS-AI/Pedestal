@file:Suppress("UNUSED_VARIABLE")

import java.net.URL

plugins {
    id("opensavvy.gradle.library")
}

kotlin {
    jvm()
    js(IR) {
        browser()
        nodejs()
    }
    iosSimulatorArm64()
    iosArm64()
    iosX64()
    linuxX64()

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.progress)
                api(KotlinX.coroutines.core)

                implementation(projects.logger)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(projects.tester)

                api(KotlinX.coroutines.test)
            }
        }
    }
}

metadata {
    name.set("Pedestal Progress (Kotlin Coroutines compatibility)")
    description.set("Coroutine-aware universal progress representation")
    homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/progress-coroutines/index.html")

    minimalCoverage.set(90)
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        includes.from("${project.projectDir}/README.md")

        sourceLink {
            localDirectory.set(file("src"))
            remoteUrl.set(URL("https://gitlab.com/opensavvy/pedestal/-/blob/main/progress-coroutines/src"))
            remoteLineSuffix.set("#L")
        }
    }
}
