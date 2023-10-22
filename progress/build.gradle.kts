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
    minimalCoverage.set(90)
}

library {
    name.set("Progress")
    description.set("Lingua franca for progress representation")
    homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/progress/index.html")
}
