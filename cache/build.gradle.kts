@file:Suppress("UNUSED_VARIABLE")

import java.net.URL

plugins {
	id("opensavvy.gradle.library")
}

kotlin {
	jvm()
	js(IR) {
		browser()
	}
	iosSimulatorArm64()
	iosArm64()
	iosX64()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(projects.state)
				api(projects.stateCoroutines)
				api(KotlinX.datetime)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
				implementation(KotlinX.coroutines.test)
				implementation(projects.stateArrow)
			}
		}
	}
}

metadata {
	name.set("Pedestal Cache")
	description.set("Multiplatform observable asynchronous cache algorithms")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/cache/index.html")
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
			remoteUrl.set(URL("https://gitlab.com/opensavvy/pedestal/-/blob/main/cache/src"))
			remoteLineSuffix.set("#L")
		}
	}
}
