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

package opensavvy.state.arrow

import arrow.core.raise.recover
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.failedWithProgress
import opensavvy.state.progressive.successfulWithProgress

@OptIn(ExperimentalProgressiveRaiseApi::class)
class ProgressiveOutcomeDslTest : PreparedSpec({

	test("Success") {
		check(progressive<String, Int> { 2 } == 2.successfulWithProgress())
	}

	test("Failure") {
		check(progressive<String, Int> { raise("test") } == "test".failedWithProgress())
	}

	test("Bind outcome success") {
		check(progressive<String, Int> { 2.successful().bind() } == 2.successfulWithProgress())
	}

	test("Bind progressive success") {
		check(progressive<String, Int> { 2.successfulWithProgress().bind() } == 2.successfulWithProgress())
	}

	@Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // that's the purpose of the test!
	test("Bind outcome failure") {
		check(progressive<String, Int> { "test".failed().bind() } == "test".failedWithProgress())
	}

	@Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // that's the purpose of the test!
	test("Bind progressive failure") {
		check(progressive<String, Int> { "test".failedWithProgress().bind() } == "test".failedWithProgress())
	}

	test("Tricky situation") {
		val result: ProgressiveOutcome<String, Int> = progressive {
			recover<ProgressiveOutcome.Unsuccessful<String>, _>(
				block = {
					raise("foo", loading(0.2))
				},
				recover = {
					1
				}
			)
		}

		// Ideally, we'd want this to be true:
		// check(result == 1.successfulWithProgress())
		// …however, for now, we can't, so we settle for this weird behavior:
		check(result == "foo".failedWithProgress(loading(0.2)))
	}
})
