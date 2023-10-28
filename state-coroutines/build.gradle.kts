@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("conventions.base")
    id("conventions.kotlin")
    id("conventions.library")
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
                api(projects.state)
                api(libs.kotlinx.coroutines.core)

                api(projects.progress)
                api(projects.progressCoroutines)

                implementation(projects.logger)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(projects.tester)

                api(libs.kotlinx.coroutines.test)
            }
        }
    }
}

coverage {
    minimalCoverage.set(80)
}

library {
    name.set("State (Coroutines compatibility)")
    description.set("Progress-aware failure states")
    homeUrl.set("https://opensavvy.gitlab.io/pedestal/api-docs/state-coroutines/index.html")
}
