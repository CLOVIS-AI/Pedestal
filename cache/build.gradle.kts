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
	}
	iosSimulatorArm64()
	iosArm64()
	iosX64()
	linuxX64()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(projects.state)
				api(projects.stateCoroutines)
				api(libs.kotlinx.datetime)

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

coverage {
	minimalCoverage.set(90)
}

library {
	name.set("Cache")
	description.set("Multiplatform observable asynchronous cache algorithms")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/api-docs/cache/index.html")
}
