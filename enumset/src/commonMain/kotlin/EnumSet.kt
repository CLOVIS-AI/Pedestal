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

import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

@PublishedApi
@ExperimentalEnumSetApi
internal fun <E : Enum<E>> enumSetOf(
	elements: Iterable<E>,
	entries: EnumEntries<E>,
): Set<E> {
	return when (entries.size) {
		0 -> emptySet()

		in 1..32 -> EnumSet32.of(elements, entries)

		// If the passed elements are already an EnumEntries, we know the user wants a set with *all* elements.
		else if elements is EnumEntries<*> -> EnumEntriesSet(entries)

		else -> elements.toSet()
	}
}

/**
 * Creates a [Set] optimized for storing elements of enumerations.
 *
 * Depending on the enumeration size, different implementations may be returned.
 *
 * ### Example
 *
 * ```kotlin
 * enum class Foo {
 *     A,
 *     B,
 *     C,
 * }
 *
 * val foo = enumSetOf(Foo.A, Foo.B)
 * ```
 */
@ExperimentalEnumSetApi
inline fun <reified E : Enum<E>> enumSetOf(
	vararg elements: E,
): Set<E> =
	enumSetOf(elements.asIterable())

/**
 * Creates a [Set] optimized for storing elements of enumerations.
 *
 * Depending on the enumeration size, different implementations may be returned.
 *
 * ### Example
 *
 * ```kotlin
 * enum class Foo {
 *     A,
 *     B,
 *     C,
 * }
 *
 * val foo = enumSetOf(Foo.A, Foo.B)
 * ```
 */
@ExperimentalEnumSetApi
inline fun <reified E : Enum<E>> enumSetOf(
	elements: Iterable<E>,
): Set<E> =
	enumSetOf(elements, enumEntries<E>())

/**
 * Copies a regular [Set] into a set optimized for enumerations.
 *
 * Depending on the enumeration size, different implementations may be returned.
 *
 * ### Example
 *
 * ```kotlin
 * enum class Foo {
 *     A,
 *     B,
 *     C,
 * }
 *
 * val foo = setOf(Foo.A, Foo.B).toEnumSet()
 * ```
 */
@ExperimentalEnumSetApi
inline fun <reified E : Enum<E>> Set<E>.toEnumSet(): Set<E> =
	enumSetOf(this, enumEntries())
