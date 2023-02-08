@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("opensavvy.gradle.versioning")

	kotlin("multiplatform")
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
				api(projects.spine)

				api(KotlinX.serialization.core)
				api(Ktor.plugins.http)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)

				implementation(KotlinX.coroutines.test)
				implementation(KotlinX.serialization.json)
			}
		}
	}
}

koverMerged.enable()

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
