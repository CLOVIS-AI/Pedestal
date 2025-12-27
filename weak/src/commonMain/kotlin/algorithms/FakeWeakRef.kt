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
import opensavvy.pedestal.weak.WeakRef

/**
 * Fake implementation of [WeakRef].
 *
 * Instead of being freed by the garbage-collector, this implementation is only
 * freed when [clear] is called.
 *
 * Use this implementation to help trigger edge cases in algorithms that use weak references.
 */
@ExperimentalWeakApi
class FakeWeakRef<T>(
	value: T
) : WeakRef<T> {
	private var value: T? = value

	override fun read(): T? =
		value

	fun clear() {
		value = null
	}

	override fun toString(): String =
		if (value == null) "FakeWeakRef.Empty" else "FakeWeakRef($value)"

	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (other !is FakeWeakRef<*>) return false

		if (value != other.value) return false

		return true
	}

	override fun hashCode(): Int {
		return value?.hashCode() ?: 0
	}
}
