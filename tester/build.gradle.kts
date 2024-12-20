plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.internal)
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

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(kotlin("test-common"))
				api(kotlin("test-annotations-common"))
			}
		}

		val jvmMain by getting {
			dependencies {
				api(kotlin("test-junit5"))
			}
		}

		val jsMain by getting {
			dependencies {
				api(kotlin("test-js"))
			}
		}
	}
}
