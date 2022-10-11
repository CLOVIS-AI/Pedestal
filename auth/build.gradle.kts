@file:Suppress("UNUSED_VARIABLE")

plugins {
	kotlin("multiplatform")
}

kotlin {
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(projects.backbone)
				api(KotlinX.datetime)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
				implementation(KotlinX.coroutines.test)
			}
		}

		val jvmMain by getting {
			dependencies {
				api("com.auth0:java-jwt:_")
			}
		}
	}
}
