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

package opensavvy.enumset

import opensavvy.enumset.datatypes.testEmptySetValidity
import opensavvy.enumset.datatypes.testFullSetValidity
import opensavvy.enumset.datatypes.testMutableSetValidity
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.prepared

@OptIn(ExperimentalEnumSetApi::class)
val MutableBitSetTest by preparedSuite {
	val emptySet by prepared { MutableBitSet32.empty() }
	val fullSet by prepared { MutableBitSet32.full() }

	testEmptySetValidity("MutableBitSet32", 32, emptySet)
	testFullSetValidity("MutableBitSet32", 32, fullSet)
	testMutableSetValidity("MutableBitSet32", 32) { MutableBitSet32.of(*it.toIntArray()) }
}
