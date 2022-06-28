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
				api(KotlinX.datetime)

				implementation(project(":logger"))
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(project(":tester"))

				api(KotlinX.coroutines.test)
			}
		}
	}
}
