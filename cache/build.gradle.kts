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

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(projects.state)
				api(KotlinX.datetime)

				implementation(projects.logger)
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
				implementation(KotlinX.coroutines.test)
			}
		}
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
			remoteUrl.set(URL("https://gitlab.com/opensavvy/pedestal/-/blob/main/cache/src"))
			remoteLineSuffix.set("#L")
		}
	}
}
