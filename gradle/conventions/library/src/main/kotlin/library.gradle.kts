package conventions

plugins {
	id("maven-publish")
	id("signing")
}

interface LibraryExtension {
	val name: Property<String>
	val description: Property<String>
	val homeUrl: Property<String>
}

val config = extensions.create<LibraryExtension>("library")

val fakeJavadocJar by tasks.registering(Jar::class) {
	description = "Fake documentation JAR for MavenCentral"
	group = "publishing"

	archiveClassifier.set("javadoc")
}

publishing {
	// region GitLab artifact registry
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
	// endregion
	// region Maven Central
	repositories {
		maven {
			name = "Sonatype"
			url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
			credentials {
				username = System.getenv("OSSRH_USERNAME")
				password = System.getenv("OSSRH_PASSWORD")
			}
		}
	}
	// endregion

	publications.withType<MavenPublication> {
		artifact(fakeJavadocJar.get())

		pom {
			name.set(config.name)
			description.set(config.description)
			url.set(config.homeUrl)

			licenses {
				license {
					name.set("Apache 2.0")
					url.set("https://gitlab.com/opensavvy/pedestal/-/raw/main/LICENSE.txt")
				}
			}
			developers {
				developer {
					id.set("clovis-ai")
					name.set("Ivan “CLOVIS” Canet")
					email.set("ivan.canet@gmail.com")
				}
			}
			scm {
				url.set("https://gitlab.com/opensavvy/pedestal")
			}
		}
	}
}


run {
	ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID") ?: return@run
	ext["signing.password"] = System.getenv("SIGNING_PASSWORD") ?: return@run
	ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_KEY_RING") ?: return@run

	signing {
		sign(publishing.publications)
	}
}
