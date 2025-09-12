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

package opensavvy.enumset.datatypes

import io.kotest.assertions.throwables.shouldThrow
import opensavvy.enumset.EnumEntriesSet
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.TestDsl
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.random.random
import opensavvy.prepared.suite.random.randomInt
import kotlin.enums.EnumEntries

fun <E : Enum<E>> SuiteDsl.testEnumSetValidity(
	name: String,
	entries: EnumEntries<E>,
	create: (Iterable<E>) -> Set<E>,
) = suite("Set $name") {
	testEmptyEnumSetValidity(name, entries) { create(emptyList()) }

	suite("One element") {
		val index by randomInt(0, entries.size)
		val value by prepared { entries[index()] }
		val set by prepared {
			create(listOf(value()))
		}

		test("isEmpty() should be false") {
			check(!set().isEmpty())
		}

		test("size should be 1") {
			check(set().size == 1)
		}

		test("toString() should be [x]") {
			check(set().toString() == "[${value()}]")
		}

		test("Iterator should have size 1") {
			val iter = set().iterator()
			check(iter.hasNext())
			check(iter.next() == value())
			check(!iter.hasNext())
		}

		test("The element should be contained") {
			check(value() in set())
		}

		test("Other elements should not be contained") {
			val value = value()
			val elements = entries
				.filter { it != value }

			for (element in elements) {
				check(element !in set())
			}
		}

		test("Creation with duplicates ignores them") {
			val value = entries.random(random.accessUnsafe())
			val set = create(listOf(value, value))
			println("Creating a set which contains the value $value twice")

			check(value in set)

			for (element in entries) {
				if (element != value) {
					check(element !in set)
				}
			}
		}
	}

	suite("Multiple elements") {
		val targetSize by randomInt(1, entries.size)
		val values by prepared {
			buildSetOfSize(targetSize(), entries)
		}
		val set by prepared {
			create(values())
		}

		test("isEmpty() should be false") {
			check(!set().isEmpty())
		}

		test("size should be correct") {
			check(set().size == targetSize())
		}

		test("toString should mention exactly all elements, in order") {
			if (set() is EnumEntriesSet) {
				check(set().toString() == values().sorted().joinToString(", ", prefix = "[", postfix = "]"))
			} else {
				println("This test does not make sense for set ${set()} (${set()::class})")
			}
		}

		test("The iterator's size should be correct") {
			val iter = set().iterator()

			repeat(targetSize()) {
				check(iter.hasNext())
				iter.next()
			}

			check(!iter.hasNext())
		}

		test("All the elements should be contained") {
			for (element in values()) {
				check(element in set())
			}
		}

		test("Other elements should be not contained") {
			val targetValues = values()
			val otherValues = entries
				.filter { it !in targetValues }

			for (otherValue in otherValues) {
				check(otherValue !in set())
			}
		}
	}

	suite("Full") {
		val set by prepared {
			create(entries)
		}
		testFullEnumSetValidity(name, entries, set)
	}
}

fun <E : Enum<E>> SuiteDsl.testEmptyEnumSetValidity(
	name: String,
	entries: EnumEntries<E>,
	create: () -> Set<E>,
) = suite("Empty $name") {
	val set by prepared {
		create()
	}

	test("isEmpty() should be true") {
		check(set().isEmpty())
	}

	@Suppress("ReplaceSizeZeroCheckWithIsEmpty")
	test("size should be 0") {
		check(set().size == 0)
	}

	test("toString() should be []") {
		check(set().toString() == "[]")
	}

	test("Iterator should be empty") {
		check(!set().iterator().hasNext())

		shouldThrow<NoSuchElementException> {
			set().iterator().next()
		}
	}

	test("No element should be contained") {
		for (element in entries) {
			check(element !in set())
		}
	}
}

fun <E : Enum<E>> SuiteDsl.testFullEnumSetValidity(
	name: String,
	entries: EnumEntries<E>,
	create: Prepared<Set<E>>,
) = suite("Full $name") {
	val set by prepared {
		create()
	}

	test("isEmpty() should be false") {
		check(!set().isEmpty())
	}

	test("size should be ${entries.size}") {
		check(set().size == entries.size)
	}

	test("toString() should be $entries") {
		check(set().toString() == entries.joinToString(", ", "[", "]"))
	}

	test("Iterator should have size ${entries.size}") {
		check(set().toList().size == entries.size)
	}

	test("All elements should be contained") {
		for (element in entries) {
			check(element in set())
		}
	}

	test("All elements should be contained, using .containAll()") {
		check(set().containsAll(entries))
	}
}

private suspend fun <E : Enum<E>> TestDsl.buildSetOfSize(
	size: Int,
	entries: EnumEntries<E>,
): Set<E> =
	buildSet {
		while (this.size < size) {
			add(entries.random(random.accessUnsafe()))
		}
	}
