package conventions

plugins {
	base
}

val appVersion: String? by project

group = "dev.opensavvy.pedestal"
version = appVersion ?: "0.0.0-DEV"

// region Check the users of the project didn't forget to rename the group

val projectPath: String? = System.getenv("CI_PROJECT_PATH")
if (projectPath != null && projectPath != "opensavvy/playgrounds/gradle" && group == "dev.opensavvy.playground") {
	error("The project is declared to be in the group '$group', which is recognized as the Gradle Playground, but it's hosted in '$projectPath', which is not the Playground. Maybe you forgot to rename the group when importing the Playground in your own project?")
}

// endregion
