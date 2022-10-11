plugins {
	kotlin("multiplatform") apply false
	id("com.palantir.git-version")
	id("org.jetbrains.kotlinx.kover")
	id("org.jetbrains.dokka")
	id("maven-publish")
}

group = "opensavvy"
version = calculateVersion()

subprojects {
	group = rootProject.group
	version = rootProject.version
}

allprojects {
	repositories {
		mavenCentral()
	}

	plugins.apply("org.jetbrains.dokka")
	plugins.apply("org.jetbrains.kotlinx.kover")
	plugins.apply("maven-publish")

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
				logger.debug("The GitLab registry is disabled because credentials are missing.")
		}
	}
}

koverMerged.enable()

fun calculateVersion(): String {
	val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
	val details = versionDetails()

	return if (details.commitDistance == 0)
		details.lastTag
	else
		"${details.lastTag}-post.${details.commitDistance}+${details.gitHash}"
}
