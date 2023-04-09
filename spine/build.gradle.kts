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
				api(projects.state)
				api(projects.stateArrow)
				api(projects.stateCoroutines)
				api(KotlinX.serialization.core)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
				implementation(projects.backbone)

				implementation(KotlinX.coroutines.test)
				implementation(KotlinX.serialization.json)
			}
		}
	}
}
