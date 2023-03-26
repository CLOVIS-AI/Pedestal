import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
	`kotlin-dsl`
}

group = "opensavvy.pedestal"

dependencies {
	implementation(project(":versioning"))
	implementation(project(":structure"))
}
