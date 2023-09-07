package opensavvy.gradle

import java.net.URL

plugins {
	id("org.jetbrains.dokka")
	id("opensavvy.gradle.versioning")
	id("maven-publish")
	id("signing")
	id("org.jetbrains.kotlinx.kover")
}

interface StructureExtension {
	val name: Property<String>
	val description: Property<String>
	val homeUrl: Property<String>
}

val config = extensions.create<StructureExtension>("metadata")

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
		externalDocumentationLink {
			url.set(URL("https://apidocs.arrow-kt.io"))
		}
	}
}

kover {
	verify {
		rule {
			name = "Minimal code coverage"
			bound {
				minValue = config.minimalCoverage.orNull
			}
		}
	}
}

val javadocJar by tasks.registering(Jar::class) {
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
		val centralUsername = System.getenv("OSSRH_USERNAME")
		val centralPassword = System.getenv("OSSRH_PASSWORD")

		if (centralUsername != null && centralPassword != null) {
			maven {
				name = "Sonatype"
				url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
				credentials {
					username = centralUsername
					password = centralPassword
				}
			}
		}
	}
	// endregion

	publications.withType<MavenPublication> {
		artifact(javadocJar.get())

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

tasks.withType<Test>().configureEach {
	maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

run {
	ext["signing.keyId"] = System.getenv("SIGNING_KEY_ID") ?: return@run
	ext["signing.password"] = System.getenv("SIGNING_PASSWORD") ?: return@run
	ext["signing.secretKeyRingFile"] = System.getenv("SIGNING_KEY_RING") ?: return@run

	signing {
		sign(publishing.publications)
	}
}
