plugins {
	`kotlin-dsl`
}

group = "opensavvy"

dependencies {
	implementation(project(":versioning"))
	implementation(project(":structure"))
}
