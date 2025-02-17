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

/**
 * A weak reference holds a reference to another object, without preventing that object from being garbage-collected.
 *
 * The [value][read] referenced by this class is eligible for garbage-collection, meaning that it could disappear
 * **at any time**. Garbage collectors are complex machinery. One should take care _not_ to write code that depends
 * on the behavior of a specific garbage collector, as that behavior can change between future versions.
 *
 * Storing `null` is technically allowed for convenience reasons, but doesn't make much sense.
 * It is not possible to distinguish between a weak reference that stores `null` and one that used store a value
 * but has been cleared.
 * Therefore, implementations are free to not store `null` values at all.
 *
 * ### Obtain instances
 *
 * The default implementations are available via the top-level [WeakRef] and [SoftRef] functions.
 * Other implementations are available in the `algorithms` subpackage.
 *
 * ### Not stable for inheritance
 *
 * This interface is not stable for inheritance. We may add new methods at any time.
 */
interface WeakRef<out T> {

	/**
	 * Attempts to read the value held by this reference.
	 *
	 * If the value was garbage-collected, which may happen at any time,
	 * this accessor returns `null`.
	 *
	 * One should not assume any additional behavior. In particular, the value may continue
	 * to be returned long after it has become unreachable from anywhere else in the program,
	 * or disappear sooner than one expected.
	 *
	 * In particular, it is not possible for this class to provide a function to check whether
	 * the value is still available or not: the value could disappear between that check and
	 * the call to this function.
	 */
	fun read(): T?

	companion object
}

/**
 * Instantiates a weak reference: a reference to a [value] that doesn't stop
 * the garbage collector from collecting it.
 *
 * The value may be accessed with [WeakRef.read].
 * However, it could disappear **at any time**.
 *
 * ### When should you use a weak reference?
 *
 * The runtime is encouraged to clear the value as soon as possible.
 *
 * This makes implementations ideal for writing mappers from an object to a more expensive one,
 * when we know that we don't need the result of the mapping as soon as the initial object
 * becomes unavailable.
 *
 * The exact behavior of the returned object is platform-specific.
 *
 * @see SoftRef For implementing caches
 */
@ExperimentalWeakApi
expect fun <T> WeakRef(value: T): WeakRef<T>

/**
 * Instantiates a soft reference: a reference to a [value] that doesn't stop
 * the garbage collector from collecting it.
 *
 * The value may be accessed with [WeakRef.read].
 * However, it could disappear **at any time**.
 *
 * ### When should you use a soft reference?
 *
 * The runtime is encouraged to keep the value for as long as possible.
 * For example, the runtime may decide to keep the value until memory pressure.
 *
 * This makes this implementation ideal for writing caches, since the value will be kept longer
 * than strictly necessary.
 *
 * The exact behavior of the returned object is platform-specific.
 *
 * @see WeakRef For implementing mappers
 */
@ExperimentalWeakApi
@Suppress("FunctionName")
expect fun <T> SoftRef(value: T): WeakRef<T>
