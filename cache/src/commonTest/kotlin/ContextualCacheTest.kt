package opensavvy.cache

import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.job
import kotlinx.coroutines.test.runTest
import opensavvy.cache.contextual.batchingCache
import opensavvy.cache.contextual.cache
import opensavvy.cache.contextual.cachedInMemory
import opensavvy.cache.contextual.expireAfter
import opensavvy.state.arrow.out
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.successful
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class ContextualCacheTest {

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

	@Test
	fun read() = runTest {
		val cache = createCache()
			.cachedInMemory(backgroundScope.coroutineContext.job)
			.expireAfter(2.minutes, backgroundScope, testClock)

		val expected = (0 until 100).toList().successful()
		val actual = cache[Identifier(even = true, odd = true), Context(0, 100)].now()

		assertEquals(expected, actual)
	}

	@Test
	fun expire() = runTest {
		val cache = createCache()
			.cachedInMemory(backgroundScope.coroutineContext.job)
			.expireAfter(2.minutes, backgroundScope, testClock)

		// Update with invalid values, so we can notice whether they are expired+queried or not
		cache.update(Identifier(even = true, odd = true), Context(0, 100), (0 until 10).toList())
		cache.update(Identifier(even = true, odd = true), Context(10, 100), (0 until 10).toList())
		cache.update(Identifier(even = true, odd = false), Context(0, 100), (0 until 10).toList())

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(0, 100)].now()
			assertEquals(expected, actual)
		}

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(10, 100)].now()
			assertEquals(expected, actual)
		}

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = false), Context(0, 100)].now()
			assertEquals(expected, actual)
		}

		// The first value should be updated, but the second one should be unchanged
		cache.expire(Identifier(even = true, odd = true), Context(0, 100))

		run {
			val expected = (0 until 100).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(0, 100)].now()
			assertEquals(expected, actual)
		}

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(10, 100)].now()
			assertEquals(expected, actual)
		}

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = false), Context(0, 100)].now()
			assertEquals(expected, actual)
		}

		// The first and second values should be updated
		cache.expire(Identifier(even = true, odd = true))

		run {
			val expected = (0 until 100).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(0, 100)].now()
			assertEquals(expected, actual)
		}

		run {
			val expected = (10 until 110).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(10, 100)].now()
			assertEquals(expected, actual)
		}

		run {
			val expected = (0 until 10).toList().successful()
			val actual = cache[Identifier(even = true, odd = false), Context(0, 100)].now()
			assertEquals(expected, actual)
		}

		// The last value should be updated
		cache.expireAll()

		run {
			val expected = (0 until 100).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(0, 100)].now()
			assertEquals(expected, actual)
		}

		run {
			val expected = (10 until 110).toList().successful()
			val actual = cache[Identifier(even = true, odd = true), Context(10, 100)].now()
			assertEquals(expected, actual)
		}

		run {
			val expected = (0 until 200 step 2).toList().successful()
			val actual = cache[Identifier(even = true, odd = false), Context(0, 100)].now()
			assertEquals(expected, actual)
		}
	}

	@Test
	fun batching() = runTest {
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
			assertEquals(expected, actual)
		}
	}
}
