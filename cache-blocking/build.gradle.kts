/*
 * Copyright (c) 2025, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

plugins {
	alias(opensavvyConventions.plugins.base)
	alias(opensavvyConventions.plugins.kotlin.library)
}

kotlin {
	jvm()

	sourceSets.commonMain.dependencies {
		api(projects.cache)

		implementation(projects.logger)
	}

	sourceSets.commonTest.dependencies {
		implementation(libs.bundles.prepared)
		implementation(libs.lincheck)
		implementation(libsCommon.kotlin.test) // Needed by Lincheck
	}
}

library {
	name.set("Cache (Blocking)")
	description.set("Blocking wrappers for Pedestal Cache")
	homeUrl.set("https://opensavvy.gitlab.io/groundwork/pedestal/api-docs/cache-blocking/index.html")

	license.set {
		name.set("Apache 2.0")
		url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
	}
}
