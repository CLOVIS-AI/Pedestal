plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
}

kotlin {
	jvm()
	js(IR) {
		browser()
		nodejs()
	}
	iosSimulatorArm64()
	iosArm64()
	iosX64()
	linuxX64()

	sourceSets.commonMain.dependencies {
		api(projects.progress)

		api(libs.arrow.core)

		implementation(projects.logger)
	}

	sourceSets.commonTest.dependencies {
		implementation(libs.bundles.prepared)
	}
}

library {
	name.set("State")
	description.set("Progress-aware failure states")
	homeUrl.set("https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/state/index.html")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}

	coverage.set(80)
}
