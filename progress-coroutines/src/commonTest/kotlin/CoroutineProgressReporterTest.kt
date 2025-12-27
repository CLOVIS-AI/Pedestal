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

package opensavvy.progress.coroutines

import kotlinx.coroutines.withContext
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.progress.Progress
import opensavvy.progress.loading
import opensavvy.progress.report.ProgressReporter

val CoroutineProgressReporterTest by preparedSuite {

	test("Report a value through the coroutine context") {
		var value: Progress? = null

		val reporter = ProgressReporter { value = it }
			.asCoroutineContext()

		withContext(reporter) {
			report(loading(0.2))
		}

		check(value == loading(0.2))
	}

	test("Report without a reporter should do nothing") {
		report(loading(0.2)) // does nothing, doesn't crash
	}

	test("Report a value using a callback") {
		var value: Progress? = null

		reportProgress({ value = it }) {
			report(loading(0.2))
		}

		check(value == loading(0.2))
	}

}
