rootProject.name = "Pedestal"

pluginManagement {
	includeBuild("gradle/conventions")
	repositories.gradlePluginPortal()
}

plugins {
	id("de.fayard.refreshVersions") version "0.51.0"
}

include(
	"state",
	"state-arrow",
	"state-coroutines",
	"progress",
	"progress-coroutines",
	"cache",
	"backbone",
	"spine",
	"spine-ktor",
	"spine-ktor:spine-ktor-client",
	"spine-ktor:spine-ktor-server",
	"logger",
	"tester",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

buildCache {
	val username = System.getenv("GRADLE_BUILD_CACHE_CREDENTIALS")?.split(':')?.get(0)
	val password = System.getenv("GRADLE_BUILD_CACHE_CREDENTIALS")?.split(':')?.get(1)

	val mainBranch: String? = System.getenv("CI_DEFAULT_BRANCH")
	val currentBranch: String? = System.getenv("CI_COMMIT_REF_NAME")
	val runningForTag = System.getenv().containsKey("CI_COMMIT_TAG")

	remote<HttpBuildCache> {
		url = uri("https://gradle.opensavvy.dev/cache/")

		if (username != null && password != null) credentials {
			this.username = username
			this.password = password
		}

		isPush = (mainBranch != null && currentBranch != null && mainBranch == currentBranch) || runningForTag
	}
}
