import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
}

kotlin {
	jvm()
	js(IR) {
		browser()
		nodejs()
	}
	iosSimulatorArm64()
	iosArm64()
	iosX64()
	linuxX64()

	sourceSets.jvmMain.dependencies {
		implementation(libs.slf4j)
	}

	sourceSets.commonTest.dependencies {
		implementation(libs.bundles.prepared)
	}
}

library {
	name.set("Logger")
	description.set("Simple multiplatform logger")
	homeUrl.set("https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/logger/index.html")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}

tasks.withType(KotlinCompilationTask::class) {
	compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
}
