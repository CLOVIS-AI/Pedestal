package conventions

import java.net.URL

plugins {
	id("org.jetbrains.dokka")
}

tasks.withType<org.jetbrains.dokka.gradle.DokkaTaskPartial>().configureEach {
	dokkaSourceSets.configureEach {
		includes.from("${project.projectDir}/README.md")

		externalDocumentationLink {
			url.set(URL("https://kotlinlang.org/api/kotlinx.coroutines/"))
		}
		externalDocumentationLink {
			url.set(URL("https://kotlinlang.org/api/kotlinx.serialization/"))
		}
		externalDocumentationLink {
			url.set(URL("https://www.slf4j.org/apidocs/index.html"))
		}
		externalDocumentationLink {
			url.set(URL("https://api.ktor.io/"))
		}
		externalDocumentationLink {
			url.set(URL("https://apidocs.arrow-kt.io"))
		}

		sourceLink {
			val path = projectDir.relativeTo(rootProject.projectDir)

			localDirectory.set(file("src"))
			remoteUrl.set(URL("https://gitlab.com/opensavvy/pedestal/-/blob/main/$path/src"))
			remoteLineSuffix.set("#L")
		}
	}
}
