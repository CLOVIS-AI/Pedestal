@file:Suppress("UNUSED_VARIABLE")

import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
	id("conventions.base")
	id("conventions.kotlin")
	id("conventions.library")
	id("conventions.documentation")
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

	sourceSets {
		val commonMain by getting
		val commonTest by getting {
			dependencies {
				implementation(projects.tester)
			}
		}

		val jvmMain by getting {
			dependencies {
				implementation(libs.slf4j)
			}
		}

		val iosMain by creating { dependsOn(commonMain) }
		val iosArm64Main by getting { dependsOn(iosMain) }
		val iosSimulatorArm64Main by getting { dependsOn(iosMain) }
		val iosX64Main by getting { dependsOn(iosMain) }
	}
}

coverage {
	minimalCoverage.set(90)
}

library {
	name.set("Pedestal Logger")
	description.set("Simple multiplatform logger")
	homeUrl.set("https://opensavvy.gitlab.io/pedestal/documentation/logger/index.html")
}

tasks.named<KotlinCompilationTask<*>>("compileKotlinIosSimulatorArm64").configure {
	compilerOptions.freeCompilerArgs.add("-opt-in=kotlinx.cinterop.ExperimentalForeignApi")
}
