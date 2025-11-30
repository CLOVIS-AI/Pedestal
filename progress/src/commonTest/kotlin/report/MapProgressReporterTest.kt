/*
 * Copyright (c) 2023-2025, OpenSavvy and contributors.
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

package opensavvy.progress.report

import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.progress.loading

@Suppress("unused")
val MapProgressReporterTest by preparedSuite {

	test("Should intercept reported values and transform them (replacing by done)") {
		var value: Progress? = null

		val reporter = ProgressReporter { value = it }
			.map { done() }

		reporter.report(loading(0.5))
		check(value == done())
	}

	test("Should intercept reported values and transform them (increasing the value)") {
		var value: Progress? = null

		val reporter = ProgressReporter { value = it }
			.map { it as Progress.Loading.Quantified; loading(it.normalized + 0.2) }

		reporter.report(loading(0.2))
		check(value == loading(0.4))
	}

	test("String representation") {
		val reporter = emptyProgressReporter()
			.reduceToInterval(0.1, 0.2)
			.map { it }

		check(reporter.toString() == "NoOpProgressReporter.reduceToInterval(0.1..0.2).map()")
	}
}
