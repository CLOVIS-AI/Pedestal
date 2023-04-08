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
            remoteUrl.set(URL("https://gitlab.com/opensavvy/pedestal/-/blob/main/progress/src"))
            remoteLineSuffix.set("#L")
        }
    }
}
