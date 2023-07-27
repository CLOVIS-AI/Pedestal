@file:Suppress("UNUSED_VARIABLE")

import java.net.URL

plugins {
	id("opensavvy.gradle.library")
}

kotlin {
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(projects.cache)

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
	name.set("Pedestal Cache (Blocking)")
	description.set("Blocking wrappers for Pedestal Cache")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/cache-blocking/index.html")
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
	dokkaSourceSets.configureEach {
		includes.from("${project.projectDir}/README.md")

		sourceLink {
			localDirectory.set(file("src"))
			remoteUrl.set(URL("https://gitlab.com/opensavvy/pedestal/-/blob/main/cache-blocking/src"))
			remoteLineSuffix.set("#L")
		}
	}
}
