/*
 * Copyright (c) 2025-2026, OpenSavvy and contributors.
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
private fun <E : Enum<E>> Iterable<E>.toBitSet(): MutableBitSet32 {
	val elements = MutableBitSet32()
	for (element in this) {
		elements.add(element.ordinal)
	}
	return elements
}

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
		return set.containsAll(elements.toBitSet())
	}

	override fun toString(): String =
		this.joinToString(", ", "[", "]")

	override fun equals(other: Any?): Boolean {
		if (other === null) return false
		if (other === this) return true
		if (other is EnumSet32<*>) return entries == other.entries && set == other.set
		if (other !is Set<*>) return false

		return size == other.size &&
			this.containsAll(other)
	}

	override fun hashCode(): Int =
		sumOf { it.hashCode() }

	companion object {

		internal fun <E : Enum<E>> of(elements: Iterable<E>, entries: EnumEntries<E>): EnumSet32<E> {
			return EnumSet32(elements.toBitSet().toSet(), entries)
		}
	}
}

@ExperimentalEnumSetApi
internal class MutableEnumSet32<E : Enum<E>> private constructor(
	private val set: MutableBitSet32,
	private val entries: EnumEntries<E>,
) : MutableSet<E> {

	init {
		require(entries.size < 32) { "A MutableEnumSet32 cannot be instantiated for this enum because it has more than 32 entries" }
	}

	override val size: Int
		get() = set.size

	override fun isEmpty(): Boolean =
		set.isEmpty()

	override fun contains(element: E): Boolean =
		set.contains(element.ordinal)

	override fun iterator(): MutableIterator<E> =
		MutableEnumSet32Iterator(set.iterator(), entries)

	override fun add(element: E): Boolean =
		set.add(element.ordinal)

	override fun remove(element: E): Boolean =
		set.remove(element.ordinal)

	override fun addAll(elements: Collection<E>): Boolean =
		set.addAll(elements.toBitSet())

	override fun removeAll(elements: Collection<E>): Boolean =
		set.removeAll(elements.toBitSet())

	override fun retainAll(elements: Collection<E>): Boolean =
		set.retainAll(elements.toBitSet())

	override fun clear() {
		set.clear()
	}

	private class MutableEnumSet32Iterator<E : Enum<E>>(
		private val iter: MutableIterator<Int>,
		private val entries: EnumEntries<E>,
	) : MutableIterator<E> {
		override fun hasNext(): Boolean =
			iter.hasNext()

		override fun next(): E =
			entries[iter.next()]

		override fun remove() {
			iter.remove()
		}
	}

	override fun containsAll(elements: Collection<E>): Boolean =
		set.containsAll(elements.toBitSet())

	override fun toString(): String =
		this.joinToString(", ", "[", "]")

	override fun equals(other: Any?): Boolean {
		if (other === null) return false
		if (other === this) return true
		if (other is MutableEnumSet32<*>) return entries == other.entries && set == other.set
		if (other !is Set<*>) return false

		return size == other.size &&
			this.containsAll(other)
	}

	override fun hashCode(): Int =
		sumOf { it.hashCode() }

	companion object {

		internal fun <E : Enum<E>> of(elements: Iterable<E>, entries: EnumEntries<E>): MutableEnumSet32<E> {
			val elementsSet = MutableBitSet32()
			for (element in elements) {
				elementsSet.add(element.ordinal)
			}

			return MutableEnumSet32(elementsSet, entries)
		}
	}
}
