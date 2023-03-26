package opensavvy.gradle

import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke

plugins {
	id("com.palantir.git-version")
}

fun calculateVersion(): String {
	val versionDetails: groovy.lang.Closure<com.palantir.gradle.gitversion.VersionDetails> by extra
	val details = versionDetails()

	return if (details.commitDistance == 0)
		details.lastTag
	else
		"${details.lastTag}-post.${details.commitDistance}+${details.gitHash}"
}

group = "opensavvy.pedestal"
version = calculateVersion()
