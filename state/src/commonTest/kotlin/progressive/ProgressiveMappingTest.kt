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

val ProgressiveMappingTest by preparedSuite {

	@Suppress("LocalVariableName") val Failed = "FAILED"

	suite("map") {
		test("Success") {
			check(ProgressiveOutcome.Success(5).map { it.toString() } == "5".successfulWithProgress())
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Failure") {
			check(ProgressiveOutcome.Failure(Failed).map { it.toString() } == Failed.failedWithProgress())
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Incomplete") {
			check(ProgressiveOutcome.Incomplete().map { it.toString() } == ProgressiveOutcome.Incomplete())
		}
	}

	suite("mapFailure") {
		test("Success") {
			check(
				ProgressiveOutcome.Success(5).mapFailure {
					@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
					it.toString()
				} == 5.successfulWithProgress()
			)
		}

		test("Failure") {
			check(ProgressiveOutcome.Failure(5).mapFailure { it.toString() } == "5".failedWithProgress())
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Incomplete") {
			check(ProgressiveOutcome.Incomplete().mapFailure { it.toString() } == ProgressiveOutcome.Incomplete())
		}
	}
}
