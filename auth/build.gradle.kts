@file:Suppress("UNUSED_VARIABLE")

plugins {
	kotlin("multiplatform")
	id("maven-publish")
}

kotlin {
	jvm()

	sourceSets {
		val commonMain by getting {
			dependencies {
				api(projects.backbone)
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

		val jvmMain by getting {
			dependencies {
				api("com.auth0:java-jwt:_")
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
