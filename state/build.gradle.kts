@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("conventions.base")
	id("conventions.kotlin")
	id("conventions.library")
	id("conventions.documentation")
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
	minimalCoverage.set(90)
}

library {
	name.set("Pedestal State")
	description.set("Progress-aware failure states")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/state/index.html")
}
