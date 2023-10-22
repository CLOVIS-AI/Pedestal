@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("conventions.base")
	id("conventions.kotlin")
	id("conventions.library")
}

kotlin {
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(projects.cache)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
				implementation(libs.kotlinx.coroutines.test)
				implementation(projects.stateArrow)
			}
		}
	}
}

library {
	name.set("Cache (Blocking)")
	description.set("Blocking wrappers for Pedestal Cache")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/cache-blocking/index.html")
}
