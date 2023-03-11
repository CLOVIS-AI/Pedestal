@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("opensavvy.gradle.library")
	kotlin("plugin.serialization")
}

kotlin {
	jvm()
	js(IR) {
		browser()
		nodejs()
	}

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(Ktor.client.core)

				implementation(projects.spineKtor)
				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)

				implementation(KotlinX.coroutines.test)
			}
		}
	}
}
