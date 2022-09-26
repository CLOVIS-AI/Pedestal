@file:Suppress("UNUSED_VARIABLE")

plugins {
	kotlin("multiplatform")
	kotlin("plugin.serialization")
}

kotlin {
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(projects.spine)

				api(Ktor.server.core)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)

				implementation(projects.spineKtorClient)

				implementation(KotlinX.coroutines.test)
				implementation(Ktor.server.testHost)
			}
		}
	}
}
