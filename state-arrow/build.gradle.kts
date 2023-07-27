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

    sourceSets {
        val commonMain by getting {
            dependencies {
                api(projects.state)
                api("io.arrow-kt:arrow-core:_")

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
    name.set("Pedestal State (Arrow compatibility)")
    description.set("Progress-aware failure states")
    homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/state-arrow/index.html")
}

kover {
    verify {
        rule {
            name = "Minimal code coverage"
            bound {
                minValue = 80
            }
        }
    }
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
    dokkaSourceSets.configureEach {
        includes.from("${project.projectDir}/README.md")

        sourceLink {
            localDirectory.set(file("src"))
            remoteUrl.set(URL("https://gitlab.com/opensavvy/pedestal/-/blob/main/state-arrow/src"))
            remoteLineSuffix.set("#L")
        }
    }
}
