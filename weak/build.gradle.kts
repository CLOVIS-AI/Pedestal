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

	sourceSets.commonTest.dependencies {
		implementation(libs.bundles.prepared)
	}
}

library {
	name.set("Weak")
	description.set("Weak references and maps for Kotlin Multiplatform")
	homeUrl.set("https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/weak/index.html")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
