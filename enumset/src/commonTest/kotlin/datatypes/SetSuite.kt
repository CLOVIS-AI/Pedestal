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
import opensavvy.prepared.suite.Prepared
import opensavvy.prepared.suite.SuiteDsl
import opensavvy.prepared.suite.prepared
import opensavvy.prepared.suite.random.nextInt
import opensavvy.prepared.suite.random.random
import opensavvy.prepared.suite.random.randomInt

fun SuiteDsl.testSetValidity(
	name: String,
	maxSize: Int,
	create: (Array<Int>) -> Set<Int>,
) = suite("Set $name") {
	run {
		val set by prepared {
			create(emptyArray())
		}

		testEmptySetValidity(name, maxSize, set)
	}

	suite("One element") {
		val value by randomInt(0, maxSize)
		val set by prepared {
			create(arrayOf(value()))
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
			val elements = (0..<maxSize)
				.filter { it != value }

			for (element in elements) {
				check(element !in set())
			}
		}

		test("Creation with an element out of range fails") {
			shouldThrow<IllegalArgumentException> {
				create(arrayOf(maxSize))
			}
		}

		test("Creation with duplicates ignores them") {
			val value = value()
			val set = create(arrayOf(value, value))
			println("Creating a set which contains the value $value twice")

			check(value in set)

			for (element in 0..<maxSize) {
				if (element != value) {
					check(element !in set)
				}
			}
		}
	}

	suite("Multiple elements") {
		val targetSize by randomInt(0, maxSize - 1)
		val values by prepared {
			val size = targetSize()
			buildSet {
				while (this.size < size) {
					add(random.nextInt(0, maxSize))
				}
			}
		}
		val set by prepared {
			create(values().toTypedArray())
		}

		test("isEmpty() should be false") {
			check(!set().isEmpty())
		}

		test("size should be correct") {
			check(set().size == targetSize())
		}

		test("toString should mention exactly all elements, in order") {
			check(set().toString() == values().sorted().joinToString(", ", prefix = "[", postfix = "]"))
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
			val otherValues = (0..<maxSize)
				.filter { it !in targetValues }

			for (otherValue in otherValues) {
				check(otherValue !in set())
			}
		}
	}

	suite("Full") {
		val set by prepared {
			create(Array(maxSize) { it })
		}
		testFullSetValidity(name, 32, set)
	}
}

fun SuiteDsl.testEmptySetValidity(
	name: String,
	maxSize: Int,
	create: Prepared<Set<Int>>,
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
		for (i in 0..<maxSize) {
			check(i !in set())
		}
	}

	test("Elements out of bound should not be contained") {
		check(maxSize !in set())
		check(maxSize + 1 !in set())
		check(maxSize + 2 !in set())
	}
}

fun SuiteDsl.testFullSetValidity(
	name: String,
	maxSize: Int,
	create: Prepared<Set<Int>>,
) = suite("Full $name") {
	val set by prepared {
		create()
	}

	test("isEmpty() should be false") {
		check(!set().isEmpty())
	}

	test("size should be $maxSize") {
		check(set().size == maxSize)
	}

	test("toString() should be [0, 1, 2, 3, …$maxSize]") {
		check(set().toString() == (0..<maxSize).joinToString(", ", "[", "]"))
	}

	test("Iterator should have size $maxSize") {
		check(set().toList().size == maxSize)
	}

	test("All elements should be contained") {
		for (i in 0..<maxSize) {
			check(i in set())
		}
	}

	test("All elements should be contained, using .containAll()") {
		check(set().containsAll((0..<maxSize).toList()))
	}

	test("Elements out of bound should not be contained") {
		check(maxSize !in set())
		check(maxSize + 1 !in set())
		check(maxSize + 2 !in set())
	}
}
