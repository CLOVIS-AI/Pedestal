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
				implementation(project(":tester"))
			}
		}

		val jvmMain by getting {
			dependencies {
				implementation("org.slf4j:slf4j-api:_")
			}
		}
	}
}
