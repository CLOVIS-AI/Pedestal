
rootProject.name = "Pedestal"

plugins {
	id("de.fayard.refreshVersions") version "0.40.1"
}

include(
	"backbone",
	"spine",
	"logger",
	"tester",
	"auth",
)

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
