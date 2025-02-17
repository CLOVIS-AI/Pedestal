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

import kotlin.reflect.KProperty

/**
 * A weak map is a map in which elements can be garbage-collected at any moment.
 *
 * The exact timing in which elements can be garbage-collected is implementation-defined.
 * Some implementations may allow garbage-collection of keys, some may allow garbage-collection of values,
 * or some entirely different behavior.
 *
 * This class expresses that an association between two values exists, but we may be capable of regenerating it
 * if it disappears, or we simply don't have an important enough need for it to keep memory.
 *
 * Unlike regular [Map], this interface doesn't allow iteration.
 * It is not possible to observe the contents of this map, since they are ever mutable
 * and elements may disappear at any time.
 * Some implementations may provide such observability features.
 *
 * ### Obtain instances
 *
 * The default implementation is available via the top-level [WeakMap] function.
 * Other implementations are available in the `algorithms` subpackage.
 */
interface WeakMap<in K, V> {

	/**
	 * Gets the value associated with [key] in this map.
	 *
	 * If no value is currently associated with [key], `null` is returned.
	 *
	 * @see Map.get Equivalent method for regular maps.
	 */
	operator fun get(key: K): V?

	/**
	 * Associates [value] to the [key].
	 *
	 * An implementation may remove this association at any time.
	 *
	 * @see MutableMap.set Equivalent method for regular maps.
	 */
	operator fun set(key: K, value: V)

	/**
	 * Checks whether a value is currently associated with [key].
	 *
	 * **Note.** Values may be removed at any-time.
	 * It is possible that a [get] call returns `null`,
	 * even if it is directly following a [contains] call that returned `true`.
	 *
	 * @see Map.contains Equivalent method for regular maps.
	 */
	@ExperimentalWeakApi
	operator fun contains(key: K): Boolean

	/**
	 * Removes [key] from this map and returns the previously-stored value.
	 *
	 * If no value was previously stored, `null` is returned (same behavior as [get], but in a single operation).
	 *
	 * @see MutableMap.remove Equivalent method for regular maps.
	 */
	@ExperimentalWeakApi
	fun remove(key: K): V?

	companion object
}

// region Constructors

/**
 * Instantiates a new, empty [WeakMap].
 *
 * The map returned by this function has weak keys: keys of the map
 * may be freed if they are not referred to elsewhere in the program.
 * When a key is freed, its value is freed as well.
 *
 * Values are strongly held: until a key is removed from the map,
 * it may never be freed by the garbage-collector.
 *
 * The way the map decides whether two keys are identical is implementation-defined.
 * For example, some implementations may use referential equality, some others may
 * use the [Any.equals] function.
 */
@ExperimentalWeakApi
expect fun <K, V> WeakMap(): WeakMap<K, V>

/**
 * Instantiates a new [WeakMap] by copying [values].
 *
 * The map returned by this function has weak keys: keys of the map
 * may be freed if they are not referred to elsewhere in the program.
 * When a key is freed, its value is freed as well.
 *
 * Values are strongly held: until a key is removed from the map,
 * it may never be freed by the garbage-collector.
 *
 * The way the map decides whether two keys are identical is implementation-defined.
 * For example, some implementations may use referential equality, some others may
 * use the [Any.equals] function.
 */
@ExperimentalWeakApi
expect fun <K, V> WeakMap(values: Map<K, V>): WeakMap<K, V>

// endregion
// region getOrXXX helpers

/**
 * Attempts to find the value associated with [key], returning [defaultValue] if none is found.
 */
@ExperimentalWeakApi
inline fun <K, V> WeakMap<K, V>.getOrDefault(key: K, defaultValue: V): V =
	get(key) ?: defaultValue

/**
 * Attempts to find the value associated with [key], returning the result of [defaultValue] if none is found.
 *
 * @see Map.getOrElse Equivalent method for regular maps.
 */
@ExperimentalWeakApi
inline fun <K, V> WeakMap<K, V>.getOrElse(key: K, defaultValue: () -> V): V =
	get(key) ?: defaultValue()

/**
 * Attempts to find the value associated with [key].
 *
 * If no value is associated with [key], calls [defaultValue],
 * stores its result back into the map as well as returns it.
 *
 * In a way, this is a sort of simple cache, where a new value is recomputed
 * if the previous one has been deleted.
 *
 * @see MutableMap.getOrPut Equivalent method for regular maps.
 */
@ExperimentalWeakApi
inline fun <K, V> WeakMap<K, V>.getOrPut(key: K, defaultValue: () -> V): V =
	get(key) ?: run {
		val new = defaultValue()
		set(key, new)
		new
	}

// endregion
// region Delegation syntax for string-based access

/**
 * Allows to use a weak map for data-oriented usage.
 *
 * ### Example
 *
 * ```kotlin
 * val map = WeakMap<String, Int>()
 *
 * var score by map
 * var age by map
 *
 * score = 5
 * println(age)
 * ```
 *
 * @see Map.getValue Equivalent method for regular maps.
 */
@ExperimentalWeakApi
inline operator fun <V> WeakMap<String, V>.getValue(thisRef: Any?, property: KProperty<*>): V? =
	get(property.name)

/**
 * Allows to use a weak map for data-oriented usage.
 *
 * ### Example
 *
 * ```kotlin
 * val map = WeakMap<String, Int>()
 *
 * var score by map
 * var age by map
 *
 * score = 5
 * println(age)
 * ```
 *
 * @see MutableMap.setValue Equivalent method for regular maps.
 */
@ExperimentalWeakApi
inline operator fun <V> WeakMap<String, V>.setValue(thisRef: Any?, property: KProperty<*>, value: V) {
	set(property.name, value)
}

// endregion
// region Collection helpers

/**
 * Sets all the values from the provided map into the current one.
 *
 * @see MutableMap.putAll Equivalent method for regular maps.
 */
@ExperimentalWeakApi
fun <K, V> WeakMap<K, in V>.setAll(from: Map<out K, V>) {
	for ((k, v) in from) {
		set(k, v)
	}
}

/**
 * Removes the specified keys from this map.
 */
@ExperimentalWeakApi
fun <K, V> WeakMap<K, V>.removeAll(from: Iterable<K>) {
	for (k in from) {
		remove(k)
	}
}

/**
 * Removes the specified associations from this map.
 */
@ExperimentalWeakApi
fun <K, V> WeakMap<K, V>.removeAll(from: Map<out K, V>) {
	for ((k, v) in from) {
		remove(k, v)
	}
}

// endregion
// region Other extensions

/**
 * Removes the association of [key] to [value] from this map.
 *
 * If [key] is associated to another value than [value], nothing happens.
 */
@ExperimentalWeakApi
fun <K, V> WeakMap<K, V>.remove(key: K, value: V) {
	if (get(key) == value) {
		remove(key)
	}
}

// endregion
