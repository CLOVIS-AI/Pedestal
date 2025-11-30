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

val MappingTest by preparedSuite {

	suite("map") {
		test("Success") {
			check(5.successful().map { it.toString() } == "5".successful())
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Failure") {
			check(5.failed().map { it.toString() } == 5.failed())
		}
	}

	suite("mapFailure") {
		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Success") {
			check(5.successful().mapFailure { it.toString() } == 5.successful())
		}

		test("Failure") {
			check(5.failed().mapFailure { it.toString() } == "5".failed())
		}
	}
}
