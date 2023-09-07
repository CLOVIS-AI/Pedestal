package conventions

plugins {
	base
}

val appVersion: String? by project

group = "dev.opensavvy.pedestal"
version = appVersion ?: "0.0.0-DEV"
