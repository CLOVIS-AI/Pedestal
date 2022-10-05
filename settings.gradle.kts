
rootProject.name = "Pedestal"

plugins {
	id("de.fayard.refreshVersions") version "0.40.1"
}

include(
	"state",
	"backbone",
	"spine",
	"spine-ktor-client",
	"spine-ktor-server",
	"logger",
	"tester",
	"auth",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
