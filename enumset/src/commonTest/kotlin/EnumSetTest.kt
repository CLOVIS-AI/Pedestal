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

@file:OptIn(ExperimentalEnumSetApi::class)

package opensavvy.enumset

import opensavvy.enumset.datatypes.testEmptyEnumSetValidity
import opensavvy.enumset.datatypes.testEnumSetValidity
import opensavvy.prepared.runner.kotest.PreparedSpec

enum class Enum0

enum class Enum2 {
	A, B,
}

enum class Enum7 {
	A, B, C, D, E, F, G,
}

enum class Enum26 {
	A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
}

enum class Enum36 {
	A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z, I0, I1, I2, I3, I4, I5, I6, I7, I8, I9,
}

class EnumSetTest : PreparedSpec({
	testEmptyEnumSetValidity("of 0 elements", Enum0.entries) { enumSetOf() }
	testEnumSetValidity("of 2 elements", Enum2.entries) { enumSetOf(it) }
	testEnumSetValidity("of 7 elements", Enum7.entries) { enumSetOf(it) }
	testEnumSetValidity("of 26 elements", Enum26.entries) { enumSetOf(it) }
	testEnumSetValidity("of 36 elements", Enum36.entries) { enumSetOf(it) }
})
