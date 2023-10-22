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
				api(projects.cache)
				api(libs.kotlinx.datetime)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
				implementation(projects.stateArrow)

				api(libs.kotlinx.coroutines.test)
			}
		}
	}
}

coverage {
	minimalCoverage.set(80)
}

library {
	name.set("Backbone")
	description.set("Layered software architecture with aggressive caching")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/backbone/index.html")
}
