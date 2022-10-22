
rootProject.name = "Pedestal"

plugins {
	id("de.fayard.refreshVersions") version "0.40.1"
}

include(
	"state",
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
