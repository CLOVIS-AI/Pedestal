@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("conventions.base")
	id("conventions.kotlin")
	id("conventions.library")
	alias(libs.plugins.kotlinx.serialization)
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
				api(libs.kotlinx.serialization.core)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
				implementation(projects.backbone)

				implementation(libs.kotlinx.coroutines.test)
				implementation(libs.kotlinx.serialization.json)
			}
		}
	}
}

library {
	name.set("Spine (DEPRECATED)")
	description.set("Multiplatform API declaration")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/spine/index.html")
}
