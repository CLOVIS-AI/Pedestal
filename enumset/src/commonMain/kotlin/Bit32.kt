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

import kotlin.jvm.JvmInline

/**
 * Immutable [Set] implementation that can store values in range `0..31`.
 *
 * To create an instance of this class, see [BitSet32.of], [BitSet32.full] and [BitSet32.empty].
 *
 * Instances of this class have the following particularities:
 * - Operations on a single element execute in `O(1)`.
 * - Memory usage is `O(1)`: exactly 32 bits when unboxed.
 */
@ExperimentalEnumSetApi
@JvmInline
value class BitSet32 private constructor(private val value: Int) : Set<Int> {

	override val size: Int
		get() = value.countOneBits()

	override fun isEmpty(): Boolean =
		value == 0

	override fun contains(element: Int): Boolean {
		if (element >= 32)
			return false

		val rank = 1 shl element
		return value and rank != 0
	}

	override fun iterator(): Iterator<Int> =
		BitSet32Iterator(this)

	override fun containsAll(elements: Collection<Int>): Boolean =
		elements.all { contains(it) } // Can probably be made faster in the future

	override fun toString(): String {
		if (value == 0) return "[]"

		return buildString {
			append('[')
			var hasPrevious = false
			for (element in this@BitSet32) {
				if (hasPrevious) {
					append(',')
					append(' ')
				}

				append(element)
				hasPrevious = true
			}
			append(']')
		}
	}

	private class BitSet32Iterator(
		private val set: BitSet32,
	) : Iterator<Int> {
		private var index = 0

		override fun hasNext(): Boolean {
			while (index < 32) {
				if (index in set) {
					return true
				}
				index++
			}
			return false
		}

		override fun next(): Int {
			check(index < 32) { "Impossible state: 'next()' was called after this iterator reached the end of the set" }

			if (index !in set) {
				throw NoSuchElementException()
			}

			return index++
		}
	}

	companion object {

		/**
		 * Creates an empty instance of [BitSet32].
		 *
		 * Usually, we use [emptySet] as a default value for empty sets. However, since [BitSet32] is a `value class`,
		 * using it in a type declared as the supertype `Set` creates some boxing, which may not be wanted
		 * in some situations.
		 *
		 * To create an empty set without boxing, use this method.
		 */
		@ExperimentalEnumSetApi
		fun empty(): BitSet32 {
			return BitSet32(0)
		}

		/**
		 * Creates a full instance of [BitSet32], which contains all elements in `0..31`.
		 */
		@ExperimentalEnumSetApi
		fun full(): BitSet32 {
			return BitSet32(-1)
		}

		/**
		 * Creates an instance of [BitSet32] that contains the given [elements].
		 *
		 * If [elements] contains duplicates, they will only be present once in the resulting set (since sets cannot contain duplicates).
		 *
		 * @throws IllegalArgumentException If an element is not in the range `0..31`.
		 */
		@ExperimentalEnumSetApi
		fun of(vararg elements: Int): BitSet32 {
			var value = 0

			for (element in elements) {
				require(element in 0..31) { "BitSet32 cannot contain the element $element which is out of range" }
				val rank = 1 shl element
				value = value or rank
			}

			return BitSet32(value)
		}
	}
}
