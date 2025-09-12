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

@ExperimentalEnumSetApi
internal class EnumSet32<E : Enum<E>> private constructor(
	private val set: BitSet32,
	private val entries: EnumEntries<E>,
) : Set<E> {

	init {
		require(entries.size < 32) { "An EnumSet32 cannot be instantiated for this enum because it has more than 32 entries" }
	}

	override val size: Int
		get() = set.size

	override fun isEmpty(): Boolean =
		set.isEmpty()

	override fun contains(element: E): Boolean =
		set.contains(element.ordinal)

	override fun iterator(): Iterator<E> =
		EnumSet32Iterator(set.iterator(), entries)

	private class EnumSet32Iterator<E : Enum<E>>(
		private val iter: Iterator<Int>,
		private val entries: EnumEntries<E>,
	) : Iterator<E> {
		override fun hasNext(): Boolean =
			iter.hasNext()

		override fun next(): E =
			entries[iter.next()]
	}

	override fun containsAll(elements: Collection<E>): Boolean {
		val elementsMask = MutableBitSet32()
		for (element in elements) {
			val ordinal = element.ordinal
			elementsMask.add(ordinal)
		}

		return set.containsAll(elementsMask)
	}

	override fun toString(): String =
		this.joinToString(", ", "[", "]")

	companion object {

		internal fun <E : Enum<E>> of(elements: Iterable<E>, entries: EnumEntries<E>): EnumSet32<E> {
			val elementsSet = MutableBitSet32()
			for (element in elements) {
				elementsSet.add(element.ordinal)
			}

			return EnumSet32(elementsSet.toSet(), entries)
		}
	}
}
