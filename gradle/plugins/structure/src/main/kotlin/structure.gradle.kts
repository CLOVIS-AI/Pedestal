package opensavvy.gradle

import java.net.URL

plugins {
	id("org.jetbrains.dokka")
	id("opensavvy.gradle.versioning")
	id("maven-publish")
	id("org.jetbrains.kotlinx.kover")
}

repositories {
	mavenCentral()
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
	dokkaSourceSets.configureEach {
		externalDocumentationLink {
			url.set(URL("https://kotlinlang.org/api/kotlinx.coroutines/"))
		}
		externalDocumentationLink {
			url.set(URL("https://kotlinlang.org/api/kotlinx.serialization/"))
		}
		externalDocumentationLink {
			url.set(URL("https://www.slf4j.org/apidocs/index.html"))
		}
		externalDocumentationLink {
			url.set(URL("https://api.ktor.io/"))
		}
	}
}

publishing {
	repositories {
		val projectId = System.getenv("CI_PROJECT_ID")
		val token = System.getenv("CI_JOB_TOKEN")
		val api = System.getenv("CI_API_V4_URL")

		if (projectId != null) {
			maven {
				name = "GitLab"
				url = uri("$api/projects/$projectId/packages/maven")

				credentials(HttpHeaderCredentials::class.java) {
					name = "Job-Token"
					value = token
				}

				authentication {
					create<HttpHeaderAuthentication>("header")
				}
			}
		}
	}
}
