plugins {
	`kotlin-dsl`
}

group = "dev.opensavvy.pedestal"

dependencies {
	implementation(project(":versioning"))
	implementation(project(":structure"))
}
