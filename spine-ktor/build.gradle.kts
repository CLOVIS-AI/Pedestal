@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("conventions.base")
	id("conventions.kotlin")
	id("conventions.library")
	alias(playgroundLibs.plugins.kotlinx.serialization)
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

				api(libs.kotlinx.serialization.core)
				api(libs.ktor.http)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)

				implementation(libs.kotlinx.coroutines.test)
				implementation(libs.kotlinx.serialization.json)
			}
		}
	}
}

library {
	name.set("Spine for Ktor (DEPRECATED)")
	description.set("Multiplatform API declaration")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/spine-ktor/index.html")
}
