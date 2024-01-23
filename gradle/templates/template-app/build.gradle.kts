plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.application)
	application
}

kotlin {
	jvm {
		withJava() // required by the application plugin
	}

	sourceSets.commonMain.dependencies {
		implementation(projects.gradle.templates.templateLib)
	}
}

application {
	mainClass.set("opensavvy.playground.app.MainKt")
}
