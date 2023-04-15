import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
	`kotlin-dsl`
}

group = "opensavvy.pedestal"

dependencies {
	implementation("com.palantir.gradle.gitversion:gradle-git-version:_")
}
