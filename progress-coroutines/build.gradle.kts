@file:Suppress("UNUSED_VARIABLE")

plugins {
    alias(opensavvyConventions.plugins.base)
    alias(opensavvyConventions.plugins.kotlin.library)
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
                api(libs.kotlinx.coroutines.core)

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

library {
    name.set("Progress (Coroutines compatibility)")
    description.set("Coroutine-aware universal progress representation")
    homeUrl.set("https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/progress-coroutines/index.html")

    license.set {
        name.set("Apache 2.0")
        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
    }

    coverage.set(90)
}
