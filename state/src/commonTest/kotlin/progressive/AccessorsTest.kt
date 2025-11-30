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

package opensavvy.state.progressive

import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.progress.loading
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful

val AccessorsTest by preparedSuite {

	@Suppress("LocalVariableName")
	val Failed = "FAILED"

	@OptIn(ExperimentalProgressiveRaiseApi::class)
	suite("Access progress of unsuccessful values") {
		test("Failure") {
			val failure = ProgressiveOutcome.Failure(Unit, loading(0.13)) as ProgressiveOutcome.Unsuccessful<Unit>
			check(failure.progress == loading(0.13))
		}

		test("Incomplete") {
			val failure = ProgressiveOutcome.Incomplete(loading(0.13)) as ProgressiveOutcome.Unsuccessful<Unit>
			check(failure.progress == loading(0.13))
		}
	}

	suite("valueOrNull") {
		test("Success") {
			check(5.successfulWithProgress().valueOrNull == 5)
		}

		test("Failure") {
			@Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION", "SENSELESS_COMPARISON")
			check(Failed.failedWithProgress().valueOrNull == null)
		}
	}

	suite("failureOrNull") {
		test("Success") {
			@Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION", "SENSELESS_COMPARISON")
			check(5.successfulWithProgress().failureOrNull == null)
		}

		test("Failure") {
			check(Failed.failedWithProgress().failureOrNull == Failed)
		}
	}

	suite("Nothing variant") {
		test("Success") {
			check((5.successfulWithProgress() as ProgressiveOutcome<Nothing, Int>).value == 5)
		}

		test("Failure") {
			check((Failed.failedWithProgress() as ProgressiveOutcome<String, Nothing>).failure == Failed)
		}
	}

	suite("Destructuring") {
		test("Success") {
			val value: ProgressiveOutcome<*, Int> = ProgressiveOutcome.Success(5, loading(0.23))
			val (outcome, progress) = value

			check(outcome == 5.successful())
			check(progress == loading(0.23))
		}

		test("Failure") {
			val value: ProgressiveOutcome<*, Int> = ProgressiveOutcome.Failure(Failed, loading(0.23))
			val (outcome, progress) = value

			check(outcome == Failed.failed())
			check(progress == loading(0.23))
		}

		test("Incomplete") {
			val value: ProgressiveOutcome<*, Int> = ProgressiveOutcome.Incomplete(loading(0.23))
			val (outcome, progress) = value

			check(outcome == null)
			check(progress == loading(0.23))
		}
	}
}
