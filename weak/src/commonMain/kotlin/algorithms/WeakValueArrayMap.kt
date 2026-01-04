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

package opensavvy.pedestal.weak.algorithms

import opensavvy.pedestal.weak.ExperimentalWeakApi
import opensavvy.pedestal.weak.SoftRef
import opensavvy.pedestal.weak.WeakMap
import opensavvy.pedestal.weak.WeakRef

@ExperimentalWeakApi
internal class WeakValueHashMapImpl<K, V>(
	private val valueGenerator: (V) -> WeakRef<V>,
) : WeakMap<K, V> {

	private val storage = HashMap<K, WeakRef<V>>()

	override fun get(key: K): V? {
		val stored = storage[key] ?: return null
		val value = stored.read()

		if (value == null) {
			storage.remove(key)
		}

		return value
	}

	override fun set(key: K, value: V) {
		storage[key] = valueGenerator(value)
	}

	@ExperimentalWeakApi
	override fun contains(key: K): Boolean =
		get(key) != null

	@ExperimentalWeakApi
	override fun remove(key: K): V? =
		storage.remove(key)?.read()

	override fun toString() = buildString {
		append("WeakValueHashMap {")

		val iter = storage.iterator()

		while (iter.hasNext()) {
			val (key, valueRef) = iter.next()
			val value = valueRef.read()

			if (value == null) {
				iter.remove()
			} else {
				append(key)
				append(" = ")
				append(value)
				append(", ")
			}
		}

		if (storage.isNotEmpty())
			deleteRange(length - 2, length)

		append("}")
	}
}

/**
 * A pure-Kotlin common [WeakMap] implementation using [WeakRef].
 *
 * The values are weakly held.
 *
 * ### Performance characteristics
 *
 * Elements are stored in a [HashMap]. Internal storage is cleared whenever a key is accessed that has a cleared value.
 * The performance of each operation is therefore the same as a [HashMap].
 *
 * @see SoftValueHashMap The same data structure, with an underlying [SoftRef].
 */
@ExperimentalWeakApi
fun <K, V> WeakValueHashMap(): WeakMap<K, V> =
	WeakValueHashMapImpl(valueGenerator = ::WeakRef)

/**
 * A pure-Kotlin common [WeakMap] implementation using [SoftRef].
 *
 * The values are weakly held.
 *
 * ### Performance characteristics
 *
 * Elements are stored in a [HashMap]. Internal storage is cleared whenever a key is accessed that has a cleared value.
 * The performance of each operation is therefore the same as a [HashMap].
 *
 * @see WeakValueHashMap The same data structure, with an underlying [WeakRef].
 */
@ExperimentalWeakApi
fun <K, V> SoftValueHashMap(): WeakMap<K, V> =
	WeakValueHashMapImpl(valueGenerator = ::SoftRef)
