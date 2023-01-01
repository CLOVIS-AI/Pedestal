@file:Suppress("UNUSED_VARIABLE")

import java.net.URL

plugins {
	kotlin("multiplatform")
}

kotlin {
	jvm()
	js(IR) {
		browser()
		nodejs()
	}

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

				api(KotlinX.coroutines.test)
			}
		}
	}
}

kover {
	verify {
		rule {
			name = "Minimal code coverage"
			bound {
				minValue = 80
			}
		}
	}
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
