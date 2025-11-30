/*
 * Copyright (c) 2022-2025, OpenSavvy and contributors.
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

package opensavvy.state

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import opensavvy.prepared.runner.testballoon.preparedSuite

/**
 * This is a series of examples of how to combine state instances together.
 */
@Suppress("unused")
val ComprehensionsTest by preparedSuite {
	test("Map values") {
		val input = flow {
			emit("5")
			delay(100)

			emit("10")
			delay(100)

			emit("test")
		}.map { it.toIntOrNull() }
			.toList()

		check(input == listOf(5, 10, null))
	}
}
