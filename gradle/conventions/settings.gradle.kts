rootProject.name = "conventions"

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}

	versionCatalogs {
		create("libs") {
			from(files("../libs.versions.toml"))
		}

		create("playgroundLibs") {
			from(files("../playground.versions.toml"))
		}
	}
}

include(
	"base",
	"root",
	"kotlin",
	"library",
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
