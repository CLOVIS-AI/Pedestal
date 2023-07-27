@file:Suppress("UNUSED_VARIABLE")

import java.net.URL

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

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(projects.state)
				api(projects.cache)
				api(KotlinX.datetime)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
				implementation(projects.stateArrow)

				api(KotlinX.coroutines.test)
			}
		}
	}
}

metadata {
	name.set("Pedestal Backbone")
	description.set("Layered software architecture with aggressive caching")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/backbone/index.html")

	minimalCoverage.set(80)
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
	dokkaSourceSets.configureEach {
		includes.from("${project.projectDir}/README.md")

		sourceLink {
			localDirectory.set(file("src"))
			remoteUrl.set(URL("https://gitlab.com/opensavvy/pedestal/-/blob/main/backbone/src"))
			remoteLineSuffix.set("#L")
		}
	}
}
