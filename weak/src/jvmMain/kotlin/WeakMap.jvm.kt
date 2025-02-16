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

package opensavvy.pedestal.weak

import java.util.*

private class JavaWeakMap<K, V>(
	private val wrapped: WeakHashMap<K, V>
) : WeakMap<K, V> {

	override fun get(key: K): V? =
		wrapped[key]

	override fun set(key: K, value: V) {
		wrapped[key] = value
	}

	@ExperimentalWeakApi
	override fun contains(key: K): Boolean =
		wrapped.containsKey(key)

	@ExperimentalWeakApi
	override fun remove(key: K): V? =
		wrapped.remove(key)

	override fun toString(): String =
		wrapped.toString()
}

/**
 * Instantiates a new, empty [WeakMap].
 *
 * This implementation is backed by a Java [WeakHashMap].
 */
@ExperimentalWeakApi
actual fun <K, V> WeakMap(): WeakMap<K, V> =
	JavaWeakMap(WeakHashMap())

/**
 * Instantiates a new [WeakMap] by copying [values].
 *
 * This implementation is backed by a Java [WeakHashMap].
 */
@ExperimentalWeakApi
actual fun <K, V> WeakMap(values: Map<K, V>): WeakMap<K, V> =
	JavaWeakMap(WeakHashMap(values))
