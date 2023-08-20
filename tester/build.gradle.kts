@file:Suppress("UNUSED_VARIABLE")

plugins {
	id("opensavvy.gradle.library")
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

metadata {
	name.set("Pedestal Tester (DEPRECATED)")
	description.set("Multiplatform test helpers")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/tester/index.html")
}
