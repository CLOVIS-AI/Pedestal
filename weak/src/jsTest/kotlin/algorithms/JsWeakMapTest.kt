/*
 * Copyright (c) 2025, OpenSavvy and contributors.
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

@file:OptIn(ExperimentalWeakApi::class)

package opensavvy.pedestal.weak.algorithms

import js.errors.TypeError
import opensavvy.pedestal.weak.ExperimentalWeakApi
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.assertions.checkThrows
import kotlin.js.json
import js.collections.WeakMap as JsWeakMap

val JsWeakMapTest by preparedSuite {

	fun <T> verify(value: T) {
		val map = JsWrappedWeakMap<T, String>(JsWeakMap())

		map[value] = "5"
		check(map[value] == "5") { "We just inserted the value $value in the map, it should still be there" }
		check(value in map) { "We just inserted the value $value in the map, it should still be there" }
	}

	test("Lists are allowed") {
		verify(listOf(1, 2, 3))
	}

	test("Objects are allowed") {
		verify(json("foo" to 1, "bar" to 2))
	}

	test("null is allowed but does not store the values") {
		val map = JsWrappedWeakMap<String?, String>(JsWeakMap())

		map[null] = "5"
		check(map[null] == null) { "'null' isn't stored" }
		check(null !in map) { "'null' isn't stored" }
	}

	test("Strings are not allowed") {
		checkThrows<TypeError> {
			verify("foo")
		}
	}

	test("Int is not allowed") {
		checkThrows<TypeError> {
			verify(5)
		}
	}

	test("Double is not allowed") {
		checkThrows<TypeError> {
			verify(5.0)
		}
	}

	test("Booleans are not allowed") {
		checkThrows<TypeError> {
			verify(true)
		}

		checkThrows<TypeError> {
			verify(false)
		}
	}

}
