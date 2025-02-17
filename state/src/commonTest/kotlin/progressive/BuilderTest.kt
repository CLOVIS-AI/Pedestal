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

import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.ExperimentalProgressApi
import opensavvy.progress.Progressive
import opensavvy.progress.loading
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful

@OptIn(ExperimentalProgressApi::class)
class BuilderTest : PreparedSpec({

	@Suppress("LocalVariableName")
	val Failed = "FAILED"

	suite("With progress") {
		test("Success") {
			check(Outcome.Success(5).withProgress(loading(0.57)) == ProgressiveOutcome.Success(5, loading(0.57)))
		}

		test("Failure") {
			check(Outcome.Failure(Failed).withProgress(loading(0.57)) == ProgressiveOutcome.Failure(Failed, loading(0.57)))
		}
	}

	suite("Convert to Progressive") {
		test("Success") {
			val initial = Progressive(5.successful(), loading(0.2))

			check(initial == initial.flatten().explode())
		}

		test("Failure") {
			val initial = Progressive(3.failed(), loading(0.1))

			check(initial == initial.flatten().explode())
		}

		test("Incomplete") {
			check(ProgressiveOutcome.Incomplete(loading(0.33)).explode() == Progressive(null, loading(0.33)))
		}
	}

	suite("Copy function") {

		fun ProgressiveOutcome<*, *>.copyProgress() =
			copy(progress = loading(0.23))

		test("Success") {
			check(ProgressiveOutcome.Success(Unit).copyProgress() == ProgressiveOutcome.Success(Unit, loading(0.23)))
		}

		test("Failure") {
			check(ProgressiveOutcome.Failure(Unit).copyProgress() == ProgressiveOutcome.Failure(Unit, loading(0.23)))
		}

		test("Incomplete") {
			check(ProgressiveOutcome.Incomplete().copyProgress() == ProgressiveOutcome.Incomplete(loading(0.23)))
		}
	}
})
