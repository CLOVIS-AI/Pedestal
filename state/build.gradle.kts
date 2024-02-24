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

		implementation(projects.logger)
	}

	sourceSets.commonTest.dependencies {
		implementation(projects.tester)

		implementation(projects.stateArrow)
	}
}

library {
	name.set("State")
	description.set("Progress-aware failure states")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/api-docs/state/index.html")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
