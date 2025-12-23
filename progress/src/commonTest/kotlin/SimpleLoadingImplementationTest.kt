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

package opensavvy.progress

import com.benwoodworth.parameterize.parameterOf
import com.benwoodworth.parameterize.parameterize
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.assertions.checkThrows

@Suppress("unused")
val SimpleLoadingImplementationTest by preparedSuite {
	suite("Constructor range validation") {
		parameterize {
			val legal by parameterOf(0.0, 0.1, 0.00001, 0.33, 1.0)

			test("The loading constructor should accept the value $legal") {
				check(loading(legal).normalized == legal)
			}
		}

		parameterize {
			val illegal by parameterOf(-1.0, 1.01, 1.000001, -0.000001, Double.MAX_VALUE, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)

			test("The loading constructor should not accept the value $illegal") {
				checkThrows<IllegalArgumentException> { loading(illegal) }
			}
		}
	}

	suite("Conversion to integer percent") {
		parameterize {
			val value by parameterOf(
				0.0 to 0,
				0.33 to 33,
				1.0 to 100,
				0.01 to 1,
				0.001 to 0,
				0.4597 to 45,
			)
			val (input, expected) = value

			test("Converting $input should give $expected") {
				check(loading(input).percent == expected)
			}
		}
	}

	suite("String conversion") {
		parameterize {
			val value by parameterOf(
				0.0 to "Loading(0%)",
				0.2 to "Loading(20%)",
				0.99 to "Loading(99%)",
				1.0 to "Loading(100%)",
			)
			val (input, expected) = value

			test("loading($input) should be represented by the string $expected") {
				check(loading(input).toString() == expected)
			}
		}
	}

	test("The hashCode implementation is correct") {
		val set = hashSetOf(
			done(),
			loading(0.0),
			loading(0.7),
			loading(0.9),
			loading(1.0),
		)

		check(done() in set)
		check(loading(0.0) in set)
		check(loading(0.7) in set)
		check(loading(0.9) in set)
		check(loading(1.0) in set)
	}

	suite("Equality") {
		test("Equal") {
			check(loading(0.0) == loading(0.0))
		}

		test("Not equal") {
			check(loading(0.0) != loading(0.7))
			check(loading(0.0) != Unit)
		}

		test("Nullability") {
			check(!loading(0.0).equals(null))
		}

		test("Same reference") {
			val l = loading(0.0)
			check(l == l)
		}
	}
}
