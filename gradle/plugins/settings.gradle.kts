dependencyResolutionManagement {
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("de.fayard.refreshVersions") version "0.51.0"
}

include(
	"versioning",
	"structure",
	"library",
)
