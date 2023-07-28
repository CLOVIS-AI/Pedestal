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
				implementation(Ktor.server.contentNegotiation)
				implementation(Ktor.client.contentNegotiation)
				implementation(Ktor.plugins.serialization.kotlinx.json)
			}
		}
	}
}

metadata {
	name.set("Spine for Ktor server (DEPRECATED)")
	description.set("Multiplatform API declaration")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/spine-ktor/spine-ktor-server/index.html")
}
