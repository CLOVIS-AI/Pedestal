@file:Suppress("UNUSED_VARIABLE")

plugins {
	kotlin("multiplatform")
	id("maven-publish")
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
				api(KotlinX.coroutines.core)
				api(KotlinX.datetime)

				implementation(project(":logger"))
			}
		}

		val commonTest by getting {
			dependencies {
				implementation(project(":tester"))

				api(KotlinX.coroutines.test)
			}
		}
	}
}

publishing {
	repositories {
		val projectId = System.getenv("CI_PROJECT_ID")
		val token = System.getenv("CI_JOB_TOKEN")

		if (projectId != null && token != null)
			maven {
				name = "GitLab"
				url = uri("https://gitlab.com/api/v4/projects/$projectId/packages/maven")

				credentials(HttpHeaderCredentials::class.java) {
					name = "Job-Token"
					value = token
				}

				authentication {
					create<HttpHeaderAuthentication>("header")
				}
			}
		else
			println("The GitLab registry is disabled because credentials are missing.")
	}
}

tasks.koverVerify {
	rule {
        name = "Minimal code coverage"
        bound {
            minValue = 80
        }
    }
}
