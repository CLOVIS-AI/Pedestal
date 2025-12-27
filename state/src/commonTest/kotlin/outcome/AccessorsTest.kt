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

val AccessorsTest by preparedSuite {

	@Suppress("LocalVariableName")
	val Failed = "FAILED"

	suite("valueOrNull") {
		test("Success") {
			check(5.successful().valueOrNull == 5)
		}

		@Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION", "SENSELESS_COMPARISON") // it's the goal of the test!
		test("Failure") {
			check(Failed.failed().valueOrNull == null)
		}
	}

	suite("failureOrNull") {
		@Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION", "SENSELESS_COMPARISON") // it's the goal of the test!
		test("Success") {
			check(5.successful().failureOrNull == null)
		}

		test("Failure") {
			check(Failed.failed().failureOrNull == Failed)
		}
	}

	suite("Nothing variants") {
		test("value") {
			check((5.successful() as Outcome<Nothing, Int>).value == 5)
		}

		test("failure") {
			check((Failed.failed() as Outcome<Any, Nothing>).failure == Failed)
		}
	}
}
