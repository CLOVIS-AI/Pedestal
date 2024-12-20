@file:Suppress("UNUSED_VARIABLE")

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
}

kotlin {
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(projects.cache)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(libs.bundles.prepared)
				implementation(libs.lincheck)
				implementation(opensavvyConventions.aligned.kotlin.test) // Needed by Lincheck
				implementation(projects.stateArrow)
			}
		}
	}
}

library {
	name.set("Cache (Blocking)")
	description.set("Blocking wrappers for Pedestal Cache")
	homeUrl.set("https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/cache-blocking/index.html")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
