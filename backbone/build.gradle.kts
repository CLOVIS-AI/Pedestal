@file:Suppress("UNUSED_VARIABLE")

plugins {
	kotlin("multiplatform")
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
				api(KotlinX.coroutines.core)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(project(":tester"))
			}
		}
	}
}
