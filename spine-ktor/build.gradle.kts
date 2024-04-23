@file:Suppress("UNUSED_VARIABLE")

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
	alias(opensavvyConventions.plugins.aligned.kotlinx.serialization)
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
				api(projects.spine)

				api(libs.kotlinx.serialization.core)
				api(libs.ktor.http)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)

				implementation(libs.kotlinx.coroutines.test)
				implementation(libs.kotlinx.serialization.json)
			}
		}
	}
}

library {
	name.set("Spine for Ktor (DEPRECATED)")
	description.set("Multiplatform API declaration")
	homeUrl.set("https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/spine-ktor/index.html")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
