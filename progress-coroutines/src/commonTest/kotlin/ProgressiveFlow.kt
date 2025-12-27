/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.progress.ExperimentalProgressApi
import opensavvy.progress.Progressive
import opensavvy.progress.done
import opensavvy.progress.loading

@OptIn(ExperimentalProgressApi::class)
val ProgressiveFlow by preparedSuite {

	suite("Capture progress") {

		test("Capture in a function") {
			val actual = captureProgress {
				report(loading(0.1))
				delay(100)
				report(loading(0.9))
				"It's done"
			}.toList()

			val expected = listOf(
				Progressive(null, loading(0.1)),
				Progressive(null, loading(0.9)),
				Progressive("It's done", done()),
			)

			check(actual == expected)
		}

		test("Capture in a flow") {
			val actual = flow {
				delay(100)
				report(loading(0.5))
				emit("It's done")
			}
				.onStart { report(loading(0.0)) }
				.onEach { report(loading(0.99)) }
				.captureProgress()
				.toList()

			val expected = listOf(
				Progressive(null, loading(0.0)),
				Progressive(null, loading(0.5)),
				Progressive(null, loading(0.99)),
				Progressive("It's done", done()),
			)

			check(actual == expected)
		}

		test("Ignore non-loading events") {
			val actual = captureProgress {
				report(loading(0.1))
				report(done())
				"It's done"
			}.toList()

			val expected = listOf(
				Progressive(null, loading(0.1)),
				Progressive("It's done", done()),
			)

			check(actual == expected)
		}

	}

}
