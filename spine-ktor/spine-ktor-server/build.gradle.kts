@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("opensavvy.gradle.library")
	kotlin("plugin.serialization")
}

kotlin {
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(Ktor.server.core)

				implementation(projects.spineKtor)
				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)

				implementation(projects.spineKtor.spineKtorClient)

				implementation(KotlinX.coroutines.test)
				implementation(KotlinX.serialization.core)
				implementation(KotlinX.serialization.json)

				implementation(Ktor.client.logging)
				implementation(Ktor.server.testHost)
				implementation("io.ktor:ktor-server-content-negotiation:_")
				implementation("io.ktor:ktor-client-content-negotiation:_")
				implementation("io.ktor:ktor-serialization-kotlinx-json:_")
			}
		}
	}
}
