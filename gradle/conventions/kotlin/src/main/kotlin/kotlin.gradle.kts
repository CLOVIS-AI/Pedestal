package conventions

plugins {
	// Currently, it is not possible to use version catalogs here…
	kotlin("multiplatform")
	id("org.jetbrains.kotlinx.kover")
}

interface KotlinConventionExtension {
	val minimalCoverage: Property<Int>
}

val config = extensions.create<KotlinConventionExtension>("coverage")

repositories {
	mavenCentral()
	google()
}

kotlin {
	jvmToolchain(17)
}

tasks.withType<Test>().configureEach {
	maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

kover {
	verify {
		rule {
			name = "Minimal code coverage"
			bound {
				minValue = config.minimalCoverage.orNull
			}
		}
	}
}
