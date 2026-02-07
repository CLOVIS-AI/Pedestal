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

@file:OptIn(ExperimentalEnumSetApi::class)

package opensavvy.enumset.datatypes

import com.benwoodworth.parameterize.parameterOf
import com.benwoodworth.parameterize.parameterize
import opensavvy.enumset.EnumEntriesSet
import opensavvy.enumset.EnumSet32
import opensavvy.enumset.ExperimentalEnumSetApi
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.TestDsl
import opensavvy.prepared.suite.assertions.checkThrows
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.random.random
import opensavvy.prepared.suite.random.randomInt
import kotlin.enums.EnumEntries
import kotlin.random.Random
import kotlin.random.nextInt

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
			if (set() is EnumEntriesSet || set() is EnumSet32) {
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

	suite("Identity") {
		val itemCounts = listOf(
			0,
			1,
			2,
			Random.nextInt(0..entries.size),
			entries.size / 2,
			entries.size,
		)

		for (itemCount in itemCounts) suite("$itemCount items") {
			/**
			 * A set of size `itemCount` with random items.
			 *
			 * This is a standard set, used as a control for this test.
			 */
			val hashSet by prepared {
				val allItems = entries.toMutableList()
				random.use { allItems.shuffle() }

				val items = HashSet<E>()
				repeat(itemCount) {
					items.add(allItems.removeLast())
				}

				items
			}

			/**
			 * The test implementation we want to test, with the same content as `hashSet`.
			 */
			val testedSet by prepared {
				create(hashSet())
			}

			test("Equals") {
				check(hashSet() == testedSet()) { "A HashSet should detect that it has the same elements as the tested set" }
				check(testedSet() == hashSet()) { "The tested set should detect that it has the same elements as the HashSet" }
			}

			test("Equals: fast-paths") {
				check(testedSet() == testedSet()) { "The tested set itself is equal to itself" }
				check(testedSet() == create(hashSet())) { "The tested set is equal to another set of the same type with the same elements" }
				check(!testedSet().equals(null))
				check(testedSet() != Any())
			}

			test("HashCode") {
				check(hashSet().hashCode() == testedSet().hashCode())
			}
		}
	}
}

fun <E : Enum<E>> SuiteDsl.testEmptyEnumSetValidity(
	name: String,
	entries: EnumEntries<E>,
	create: () -> Set<E>,
) {
	val emptySet by prepared {
		create()
	}
	testEmptyEnumSetValidity(name, entries, emptySet)
}

fun <E : Enum<E>> SuiteDsl.testEmptyEnumSetValidity(
	name: String,
	entries: EnumEntries<E>,
	create: Prepared<Set<E>>,
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

		checkThrows<NoSuchElementException> {
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

fun <E : Enum<E>> SuiteDsl.testMutableEnumSetValidity(
	name: String,
	entries: EnumEntries<E>,
	create: (Iterable<E>) -> MutableSet<E>,
) = suite("MutableSet $name") {
	/**
	 * Shuffled `entries` to easily test non-deterministic order.
	 *
	 * Each test will see the elements in a different order.
	 */
	val shuffledEntries by prepared {
		val result = entries.toMutableList()
		random.use { r -> result.shuffle(r) }
		result
	}

	testEnumSetValidity(name, entries, create)

	suite("clear") {
		val clearedSet by prepared {
			val set = create(listOf(shuffledEntries()[0], shuffledEntries()[1]))
			set.clear()
			set
		}

		testEmptyEnumSetValidity(name, entries, clearedSet)
	}

	suite("add") {
		test("Add an element to the empty set") {
			val set = create(emptyList())
			val element = entries[entries.size / 2]
			check(set.add(element))
			check(element in set)
			check(set.size == 1)
		}

		if (entries.size >= 2) {
			test("Add an element to a set of one element") {
				val set = create(listOf(shuffledEntries()[0]))
				val element = shuffledEntries()[1]
				check(set.add(element))
				check(element in set)
				check(shuffledEntries()[0] in set)
				check(set.size == 2)
			}
		}

		if (entries.size >= 3) {
			test("Add an element to set with two elements") {
				val set = create(listOf(shuffledEntries()[0], shuffledEntries()[1]))
				val element = shuffledEntries()[2]
				check(set.add(element))
				check(element in set)
				check(shuffledEntries()[0] in set)
				check(shuffledEntries()[1] in set)
				check(set.size == 3)
			}
		}

		test("Add an element that is already present") {
			val alreadyInSet = shuffledEntries()[0]
			val element = shuffledEntries()[1]
			val set = create(listOf(alreadyInSet, element))
			check(!set.add(element))
			check(alreadyInSet in set)
			check(element in set)
			check(set.size == 2)
		}
	}

	suite("addAll") {
		parameterize {
			val currentSize by parameterOf(0, 1, 2, 12.coerceAtMost(entries.size - 1), entries.size - 1)

			val set by prepared {
				create(buildSetOfSize(currentSize, entries))
			}

			val addingSize by parameterOf(0, 1, entries.size.coerceAtMost(4))

			val addingValues by prepared {
				buildSetOfSize(addingSize, entries)
			}

			val addingSet by parameterOf(
				prepared("set of any type") {
					addingValues()
				},
				prepared("set of same type") {
					create(addingValues())
				}
			)

			test("Adding $addingSize elements to a set of size $currentSize (${addingSet.name})") {
				val set = set()

				val willBeAdded = addingSet().filter { it !in set }

				check(set.addAll(addingSet()) == willBeAdded.isNotEmpty())
				check(set.size == currentSize + willBeAdded.size)
			}
		}
	}

	suite("remove") {
		test("Remove an element from the empty set") {
			val set = create(emptyList())
			val element = shuffledEntries()[0]
			check(!set.remove(element))
			check(set.isEmpty())
		}

		test("Remove the only element in a set") {
			val element = shuffledEntries()[0]
			val set = create(listOf(element))
			check(set.remove(element))
			check(set.isEmpty())
		}

		if (entries.size >= 3) {
			test("Remove an element that is not contained in the set") {
				val set = create(listOf(shuffledEntries()[0], shuffledEntries()[1]))
				val element = shuffledEntries()[2]
				check(!set.remove(element))
				check(!set.isEmpty())
				check(set.size == 2)
			}
		}

		if (entries.size >= 3) {
			test("Remove an element in a set with multiple elements") {
				val element = shuffledEntries()[2]
				val set = create(listOf(shuffledEntries()[0], shuffledEntries()[1], element))
				check(set.remove(element))
				check(!set.isEmpty())
				check(set.size == 2)
			}
		}
	}

	suite("removeAll") {
		parameterize {
			val removingSize by parameterOf(0, 1, entries.size.coerceAtMost(4))

			val currentSize by parameterOf(0, 1, 2, 12.coerceAtMost(entries.size - removingSize), entries.size - removingSize)

			val set by prepared {
				create(buildSetOfSize(currentSize, entries))
			}

			val removingValues by prepared {
				buildSetOfSize(removingSize, entries)
			}

			val removingSet by parameterOf(
				prepared("set of any type") {
					removingValues()
				},
				prepared("set of same type") {
					create(removingValues())
				}
			)

			test("Removing $removingSize elements from a set of size $currentSize (${removingSet.name})") {
				val set = set()

				val willBeRemoved = removingSet().filter { it in set }

				check(set.removeAll(removingSet()) == willBeRemoved.isNotEmpty())
				check(set.size == currentSize - willBeRemoved.size)
			}
		}
	}

	if (entries.size >= 3) {
		suite("Iterator") {
			test("Remove an item while iterating") {
				val set = create(listOf(entries[0], entries[1], entries[2]))
				println("Iterating through set $set")
				val iter = set.iterator()

				check(iter.hasNext())
				check(iter.next() == entries[0])

				check(iter.hasNext())
				iter.remove()

				println("After removal: $set")

				check(iter.hasNext())
				check(iter.next() == entries[2])
			}
		}
	}

	if (entries.size >= 3) {
		suite("retainAll") {
			test("Remove all elements") {
				val set = create(listOf(shuffledEntries()[0], shuffledEntries()[1], shuffledEntries()[2]))

				check(set.retainAll(emptySet()))
				check(set.isEmpty())
			}

			if (entries.size >= 4) {
				test("Remove a single element") {
					val element1 = shuffledEntries()[0]
					val element2 = shuffledEntries()[1]
					val element3 = shuffledEntries()[2]
					val element4 = shuffledEntries()[3]
					val set = create(listOf(element1, element2, element3))

					check(set.retainAll(setOf(element4, element1, element3)))
					check(!set.isEmpty())
					check(set.size == 2)
				}
			}

			test("Remove no elements") {
				val element1 = shuffledEntries()[0]
				val element2 = shuffledEntries()[1]
				val element3 = shuffledEntries()[2]
				val set = create(listOf(element1, element2, element3))

				check(!set.retainAll(setOf(element1, element2, element3)))
				check(!set.isEmpty())
				check(set.size == 3)
			}
		}
	}
}
