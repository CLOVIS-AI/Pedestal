package conventions

plugins {
	base
}

val appVersion: String? by project

group = "opensavvy.playground"
version = appVersion ?: "0.0.0-DEV"
