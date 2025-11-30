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

package opensavvy.state.outcome

import opensavvy.prepared.runner.testballoon.preparedSuite

val ConditionalTest by preparedSuite {

	@Suppress("LocalVariableName")
	val Failed = "FAILED"

	suite("onSuccess") {
		test("Success") {
			var test = false

			Outcome.Success(5).onSuccess {
				test = true
			}

			check(test)
		}

		test("Failure") {
			var test = false

			Outcome.Failure(Failed).onSuccess {
				test = true
			}

			check(!test)
		}
	}

	suite("onFailure") {
		test("Success") {
			var test = false

			Outcome.Success(5).onFailure {
				test = true
			}

			check(!test)
		}

		test("Failure") {
			var test = false

			Outcome.Failure(Failed).onFailure {
				test = true
			}

			check(test)
		}
	}
}
