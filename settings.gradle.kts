
rootProject.name = "Pedestal"

plugins {
	id("de.fayard.refreshVersions") version "0.40.1"
}

include(
	"backbone",
	"logger",
	"tester",
)
