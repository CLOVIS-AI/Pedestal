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
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.progress.loading

@Suppress("unused")
val IntervalReduceProgressReporterTest by preparedSuite {
	test("Cannot create an invalid range") {
		checkThrows<IllegalArgumentException> {
			emptyProgressReporter().reduceToInterval(0.7, 0.2)
		}
	}

	test("Reduce with the range syntax") {
		var value: Progress? = null

		val reporter = ProgressReporter { value = it }
			.reduceToInterval(0.2..0.4)

		reporter.report(loading(0.1))
		check(value == loading(0.22))
	}

	test("Reduce with the min-max syntax") {
		var value: Progress? = null

		val reporter = ProgressReporter { value = it }
			.reduceToInterval(0.2, 0.4)

		reporter.report(loading(0.1))
		check(value == loading(0.22))
	}

	test("Reducing the 'done' event should return the range maximum") {
		var value: Progress? = null

		val reporter = ProgressReporter { value = it }
			.reduceToInterval(0.2..0.4)

		reporter.report(done())
		check(value == loading(0.4))
	}

	test("Reducing an unquantified loading event should return the range middle") {
		var value: Progress? = null

		val reporter = ProgressReporter { value = it }
			.reduceToInterval(0.2..0.4)

		reporter.report(loading())
		check(value == loading(0.3))
	}

	test("Reducing 0 should give the range minimum") {
		var value: Progress? = null

		val reporter = ProgressReporter { value = it }
			.reduceToInterval(0.2..0.4)

		reporter.report(loading(0.0))
		check(value == loading(0.2))
	}

	test("Reducing 1 should give the range maximum") {
		var value: Progress? = null

		val reporter = ProgressReporter { value = it }
			.reduceToInterval(0.2..0.4)

		reporter.report(loading(1.0))
		check(value == loading(0.4))
	}
}
