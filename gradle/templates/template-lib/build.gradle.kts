plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
}

kotlin {
	jvm()
	js {
		browser()
		nodejs()
	}
	linuxX64()
	iosArm64()
	iosSimulatorArm64()
	iosX64()

	sourceSets.commonTest.dependencies {
		implementation(opensavvyConventions.aligned.kotlin.test.annotations)
		implementation(opensavvyConventions.aligned.kotlin.test.common)
	}

	sourceSets.jvmTest.dependencies {
		implementation(opensavvyConventions.aligned.kotlin.test.junit5)
	}

	sourceSets.jsTest.dependencies {
		implementation(opensavvyConventions.aligned.kotlin.test.js)
	}
}

library {
	name.set("Playground Core")
	description.set("Project template with configured MavenCentral publication")
	homeUrl.set("https://gitlab.com/opensavvy/playgrounds/gradle")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}

// region Publication test
// Even though this module is included in all repositories that import the Playground, we
// don't want to always publish this template.

val appGroup: String? by project

if (appGroup != "dev.opensavvy.playground") {
	tasks.configureEach {
		if (name.startsWith("publish")) {
			onlyIf("Publishing is only enabled when built as part of the Playground") { false }
		}
	}
}

// endregion
