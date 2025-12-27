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

package opensavvy.state.coroutines

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.progress.coroutines.report
import opensavvy.progress.loading
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome

private object Error

val CaptureProgressTest by preparedSuite {

	test("captureFromFlow") {
		val actual = flow {
			report(loading(0.2))
			report(loading(0.5))
			emit(Outcome.Failure(Error))
			report(loading(0.7))
			emit(Outcome.Success(5))
		}.captureProgress()
			.toList()

		val expect = listOf(
			ProgressiveOutcome.Incomplete(loading(0.2)),
			ProgressiveOutcome.Incomplete(loading(0.5)),
			ProgressiveOutcome.Failure(Error),
			ProgressiveOutcome.Incomplete(loading(0.7)),
			ProgressiveOutcome.Success(5),
		)

		check(expect == actual)
	}

	test("captureFromBlock") {
		val actual = captureProgress {
			report(loading(0.2))
			report(loading(0.7))
			Outcome.Success(5)
		}.toList()

		val expect = listOf(
			ProgressiveOutcome.Incomplete(loading(0.2)),
			ProgressiveOutcome.Incomplete(loading(0.7)),
			ProgressiveOutcome.Success(5),
		)

		check(expect == actual)
	}
}
