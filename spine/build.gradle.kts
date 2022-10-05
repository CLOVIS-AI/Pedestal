@file:Suppress("UNUSED_VARIABLE")

plugins {
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
				api(projects.state)
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
