/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
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

package opensavvy.pedestal.weak.algorithms

import opensavvy.pedestal.weak.ExperimentalWeakApi
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.TestDsl
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.random.nextBoolean
import opensavvy.prepared.suite.random.nextInt
import opensavvy.prepared.suite.random.random
import opensavvy.prepared.suite.random.randomInt

@Suppress("NAME_SHADOWING")
@OptIn(ExperimentalWeakApi::class)
class WeakKeyArrayMapTest : PreparedSpec({

	// region Helpers to fake weak references used as keys during these tests

	val keys: Prepared<HashMap<Int, FakeWeakRef<Int>>> by prepared {
		HashMap()
	}

	suspend fun TestDsl.key(key: Int): FakeWeakRef<Int> =
		checkNotNull(keys()[key])

	// endregion

	val map: Prepared<WeakKeyMapImpl<Int, String>> by prepared {
		val keys = keys()

		WeakKeyMapImpl(
			keyGenerator = { key ->
				FakeWeakRef(key)
					.also { keys[key] = it }
			},
			keysAreTheSame = { a, b -> a == b }
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
			key(5).clear()
			check(map[5] == null)
		}

		test("A GC-deleted value should not be contained") {
			val map = map()

			map[5] = "5"
			key(5).clear()
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
					key(key).clear()
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

})
