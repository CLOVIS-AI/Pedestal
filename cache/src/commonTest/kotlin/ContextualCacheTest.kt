/*
 * Copyright (c) 2023-2025, OpenSavvy and contributors.
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

package opensavvy.cache

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import opensavvy.cache.contextual.batchingCache
import opensavvy.cache.contextual.cache
import opensavvy.cache.contextual.cachedInMemory
import opensavvy.cache.contextual.expireAfter
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.backgroundScope
import opensavvy.prepared.suite.clock
import opensavvy.prepared.suite.time
import opensavvy.state.arrow.out
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.successful
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

private val data = generateSequence(0) { it + 1 }

private data class Identifier(
	val even: Boolean,
	val odd: Boolean,
)

private data class Context(
	val startAt: Int,
	val limit: Int,
)

private fun createCache() = cache<Identifier, Context, Nothing, List<Int>> { id, context ->
	out {
		data
			.drop(context.startAt)
			.filter { id.even || it % 2 != 0 }
			.filter { id.odd || it % 2 != 1 }
			.take(context.limit)
			.toList()
	}
}

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
val ContextualCacheTest by preparedSuite {

	test("Read") {
		val cache = createCache()
			.cachedInMemory(backgroundScope.coroutineContext.job)
			.expireAfter(2.minutes, backgroundScope, time.clock)

		val expected = (0 until 100).toList().successful()
		val actual = cache[Identifier(even = true, odd = true), Context(0, 100)].now()

		check(actual == expected)
	}

	test("Expire") {
		val cache = createCache()
			.cachedInMemory(backgroundScope.coroutineContext.job)
			.expireAfter(2.minutes, backgroundScope, time.clock)

		// Update with invalid values, so we can notice whether they are expired+queried or not
		cache.update(Identifier(even = true, odd = true), Context(0, 100), (0 until 10).toList())
		cache.update(Identifier(even = true, odd = true), Context(10, 100), (0 until 10).toList())
		cache.update(Identifier(even = true, odd = false), Context(0, 100), (0 until 10).toList())

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(0, 100)].now()
			check(expected == actual)
		}

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(10, 100)].now()
			check(expected == actual)
		}

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = false), Context(0, 100)].now()
			check(expected == actual)
		}

		// The first value should be updated, but the second one should be unchanged
		cache.expire(Identifier(even = true, odd = true), Context(0, 100))

		run {
			val expected = (0 until 100).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(0, 100)].now()
			check(expected == actual)
		}

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(10, 100)].now()
			check(expected == actual)
		}

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = false), Context(0, 100)].now()
			check(expected == actual)
		}

		// The first and second values should be updated
		cache.expire(Identifier(even = true, odd = true))

		run {
			val expected = (0 until 100).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(0, 100)].now()
			check(expected == actual)
		}

		run {
			val expected = (10 until 110).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(10, 100)].now()
			check(expected == actual)
		}

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = false), Context(0, 100)].now()
			check(expected == actual)
		}

		// The last value should be updated
		cache.expireAll()

		run {
			val expected = (0 until 100).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(0, 100)].now()
			check(expected == actual)
		}

		run {
			val expected = (10 until 110).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(10, 100)].now()
			check(expected == actual)
		}

		run {
			val expected = (0 until 200 step 2).toList().successful()
			val actual = cache[Identifier(even = true, odd = false), Context(0, 100)].now()
			check(expected == actual)
		}
	}

	test("Batching") {
		val initial = createCache()

		@Suppress("RemoveExplicitTypeArguments") // IDEA is wrong, they are necessary
		val cache = batchingCache<Identifier, Context, Nothing, List<Int>>(backgroundScope) { request ->
			for ((id, context) in request) {
				emitAll(
					initial[id, context]
						.map { Triple(id, context, it) }
				)
			}
		}

		cache.expire(Identifier(even = true, odd = true))
		cache.expire(Identifier(even = true, odd = false), Context(0, 100))
		cache.expireAll()
		cache.update(Identifier(even = false, odd = true), Context(0, 1), listOf(1))

		run {
			val expected = (0 until 200 step 2).toList().successful()
			val actual = cache[Identifier(even = true, odd = false), Context(0, 100)].now()
			check(expected == actual)
		}
	}

}
