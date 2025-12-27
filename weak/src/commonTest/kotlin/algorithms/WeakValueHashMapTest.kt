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

import opensavvy.pedestal.weak.ExperimentalWeakApi
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.TestDsl
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.random.nextBoolean
import opensavvy.prepared.suite.random.nextInt
import opensavvy.prepared.suite.random.random
import opensavvy.prepared.suite.random.randomInt

val WeakValueHashMapTest by preparedSuite {

	// region Helpers to fake weak references used as keys during these tests

	val values: Prepared<HashMap<String, FakeWeakRef<String>>> by prepared {
		HashMap()
	}

	suspend fun TestDsl.value(value: String): FakeWeakRef<String> =
		checkNotNull(values()[value])

	// endregion

	val map: Prepared<WeakValueHashMapImpl<Int, String>> by prepared {
		val values = values()

		WeakValueHashMapImpl(
			valueGenerator = { value ->
				FakeWeakRef(value)
					.also { values[value] = it }
			}
		)
	}

	suite("A single element is stored") {
		test("Values should be stored: get") {
			val map = map()

			map[5] = "5"
			check(map[5] == "5")
		}

		test("Values should be stored: contains") {
			val map = map()

			map[5] = "5"
			check(5 in map)
		}

		test("Setting the value again overwrites it") {
			val map = map()

			map[5] = "SHOULD BE OVERWRITTEN"
			map[5] = "EXPECTED VALUE"
			check(map[5] == "EXPECTED VALUE")
		}

		test("Cannot get an element that isn't part of the map") {
			val map = map()

			map[5] = "5"
			check(map[6] == null)
		}

		test("An element that isn't part of the map isn't contained") {
			val map = map()

			map[5] = "5"
			check(6 !in map)
		}
	}

	suite("A single element was stored") {
		test("Getting a manually-deleted value should return null") {
			val map = map()

			map[5] = "5"
			map.remove(5)
			check(map[5] == null)
		}

		test("A manually-deleted value should not be contained") {
			val map = map()

			map[5] = "5"
			map.remove(5)
			check(5 !in map)
		}

		test("Getting a GC-deleted value should return null") {
			val map = map()

			map[5] = "5"
			value("5").clear()
			check(5 !in map)
		}

		test("A GC-deleted value should not be contained") {
			val map = map()

			map[5] = "5"
			value("5").clear()
			check(5 !in map)
		}
	}

	suite("Brute-forcing complex situations") {
		// This is a test generator to create weird situations.
		// If this fails, extract a single test that tests that specific situation.

		/**
		 * How large will the map-under-test be?
		 */
		val mapSize by randomInt(2, 100)

		/**
		 * The key that will be used for the assertion.
		 */
		val testKey by randomInt(0, 1_000)

		/**
		 * Other keys that will be added to the map.
		 */
		val generatedKeys by prepared {
			List(mapSize()) { random.nextInt(0, 1_000) }
		}

		/**
		 * Adds many keys to the map.
		 */
		val addKeys by prepared {
			val map = map()

			for (key in generatedKeys()) {
				map[key] = key.toString()
			}
		}

		/**
		 * Deletes ~50% of the added keys.
		 */
		val deleteSomeKeys by prepared {
			for (key in generatedKeys()) {
				if (key != testKey() && random.nextBoolean()) {
					println("Deleting key $key")
					value(key.toString()).clear()
				}
			}
		}

		suite("Get a stored value") {
			repeat(100) {
				test("Test #$it") {
					val map = map()
					addKeys()
					map[testKey()] = "TEST"
					deleteSomeKeys()
					check(map[testKey()] == "TEST")
				}
			}
		}
	}

	suite("toString") {
		test("Empty") {
			check(map().toString() == "WeakValueHashMap {}")
		}

		test("Non-empty") {
			val map = map()

			map[1] = "1"
			map[2] = "2"
			map[3] = "3"

			check(map.toString() == "WeakValueHashMap {1 = 1, 2 = 2, 3 = 3}")
		}
	}

}
