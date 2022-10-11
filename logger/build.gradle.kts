@file:Suppress("UNUSED_VARIABLE")

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
