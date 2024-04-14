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
				api(projects.state)
				api(projects.stateArrow)
				api(projects.stateCoroutines)
				api(libs.kotlinx.serialization.core)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
				implementation(projects.backbone)

				implementation(libs.kotlinx.coroutines.test)
				implementation(libs.kotlinx.serialization.json)
			}
		}
	}
}

library {
	name.set("Spine (DEPRECATED)")
	description.set("Multiplatform API declaration")
	homeUrl.set("https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/spine/index.html")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
