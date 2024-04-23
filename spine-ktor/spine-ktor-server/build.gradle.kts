@file:Suppress("UNUSED_VARIABLE")

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
	alias(opensavvyConventions.plugins.aligned.kotlinx.serialization)
}

kotlin {
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(libs.ktor.server.core)

				implementation(projects.spineKtor)
				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)

				implementation(projects.spineKtor.spineKtorClient)

				implementation(libs.kotlinx.coroutines.test)
				implementation(libs.kotlinx.serialization.core)
				implementation(libs.kotlinx.serialization.json)

				implementation(libs.ktor.server.testHost)
				implementation(libs.ktor.server.contentNegotiation)
				implementation(libs.ktor.client.contentNegotiation)
				implementation(libs.ktor.client.logging)
				implementation(libs.ktor.kotlinxJson)
			}
		}
	}
}

library {
	name.set("Spine for Ktor server (DEPRECATED)")
	description.set("Multiplatform API declaration")
	homeUrl.set("https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/spine-ktor/spine-ktor-server/index.html")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
