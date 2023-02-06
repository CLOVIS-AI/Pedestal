
rootProject.name = "Pedestal"

plugins {
	id("de.fayard.refreshVersions") version "0.51.0"
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
