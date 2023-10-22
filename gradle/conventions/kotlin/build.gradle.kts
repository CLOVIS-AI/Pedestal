plugins {
	`kotlin-dsl`
}

group = "conventions"

dependencies {
	implementation(playgroundLibs.gradle.kotlin)
	implementation(libs.gradle.kover)
}
