import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
	`kotlin-dsl`
}

group = "opensavvy.pedestal"

dependencies {
	implementation(project(":versioning"))
	implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:_")
	implementation("org.jetbrains.dokka:dokka-gradle-plugin:_")
	implementation("org.jetbrains.kotlinx:kover:_")
}
