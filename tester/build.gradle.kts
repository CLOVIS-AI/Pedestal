@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("opensavvy.gradle.versioning")

	kotlin("multiplatform")
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

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(kotlin("test-common"))
				api(kotlin("test-annotations-common"))

				implementation("ch.qos.logback:logback-classic:_")
			}
		}

		val jvmMain by getting {
			dependencies {
				api(kotlin("test-junit"))
			}
		}

		val jsMain by getting {
			dependencies {
				api(kotlin("test-js"))
			}
		}

		val iosMain by creating
		val iosArm64Main by getting { iosMain.dependsOn(this) }
		val iosSimulatorArm64Main by getting { iosMain.dependsOn(this) }
		val iosX64Main by getting { iosMain.dependsOn(this) }
	}
}
