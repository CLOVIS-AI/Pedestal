@file:Suppress("UNUSED_VARIABLE")

import java.net.URL

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
		val commonMain by getting
		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
			}
		}

		val jvmMain by getting {
			dependencies {
				implementation("org.slf4j:slf4j-api:_")
			}
		}

		val iosMain by creating { dependsOn(commonMain) }
		val iosArm64Main by getting { dependsOn(iosMain) }
		val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
		val iosX64Main by getting { dependsOn(iosMain) }
	}
}

kover {
	verify {
		rule {
			name = "Minimal code coverage"
			bound {
				minValue = 90
			}
		}
	}
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
	dokkaSourceSets.configureEach {
		includes.from("${project.projectDir}/README.md")

		sourceLink {
			localDirectory.set(file("src"))
			remoteUrl.set(URL("https://gitlab.com/opensavvy/pedestal/-/blob/main/logger/src"))
			remoteLineSuffix.set("#L")
		}
	}
}
