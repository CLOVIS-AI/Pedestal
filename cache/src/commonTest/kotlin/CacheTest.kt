@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import opensavvy.cache.BatchingCacheAdapter.Companion.batchingCache
import opensavvy.cache.CacheAdapter.Companion.cache
import opensavvy.cache.ExpirationCache.Companion.expireAfter
import opensavvy.cache.MemoryCache.Companion.cachedInMemory
import opensavvy.logger.LogLevel
import opensavvy.logger.Logger.Companion.debug
import opensavvy.logger.Logger.Companion.info
import opensavvy.logger.loggerFor
import opensavvy.state.*
import opensavvy.state.Progression.Companion.loading
import opensavvy.state.ProgressionReporter.Companion.report
import opensavvy.state.outcome.*
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.firstValue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class CacheTest {

	private val log = loggerFor(this).apply {
		level = LogLevel.TRACE
	}

	private data class IntId(val id: Int) {
		override fun toString() = "Id($id)"
	}

	private fun adapter() = cache<IntId, Int> {
		log.debug(it) { "Requesting" }
		delay(100)
		report(loading(0.2))
		delay(10)
		ensureValid(it.id >= 0) { "Only positive integers are allowed: found ${it.id}" }
		it.id
	}

	private suspend fun testCache(cache: Cache<IntId, Int>) {
		log.info { "Regular access" }
		val zero = cache[IntId(0)]
		val one = cache[IntId(1)]
		val minus = cache[IntId(-1)]

		assertEquals(0, zero.firstValue().orNull())
		assertEquals(1, one.firstValue().orNull())
		assertEquals(null, minus.firstValue().orNull())
	}

	private suspend fun testUpdateExpire(cache: Cache<IntId, Int>) {
		log.info { "Checking normal behavior" }
		assertEquals(0, cache[IntId(0)].firstValue().orNull())

		log.info { "Overwriting with a different value" }
		cache.update(IntId(0), 5)
		assertEquals(5, cache[IntId(0)].firstValue().orNull())

		log.info { "Expiring the value re-downloads and replaces our fake value" }
		cache.expire(IntId(0))
		assertEquals(0, cache[IntId(0)].firstValue().orNull())
	}

	private suspend fun testAutoExpiration(cache: Cache<IntId, Int>) {
		log.info { "Adding 5 to the cache to make updates visible" }
		assertEquals(0, cache[IntId(0)].firstValue().orThrow())
		cache.update(IntId(0), 5)
		assertEquals(5, cache[IntId(0)].firstValue().orThrow())

		log.info { "Waiting for the cache to correct itself" }
		assertEquals(0,
		             cache[IntId(0)]
			             .onEach { log.debug(it) { "Found new value" } }
			             .drop(1) // Skip the bad value we inserted
			             .firstValue()
			             .orThrow()
		)
	}

	@Test
	fun rawAdapter() = runTest {
		val cache = adapter()

		testCache(cache)
	}

	@Test
	fun infiniteMemoryCache() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext)

		testCache(cache)

		currentCoroutineContext().cancelChildren()
	}

	@Test
	fun infiniteMemoryCacheUpdateExpire() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext)

		testUpdateExpire(cache)

		currentCoroutineContext().cancelChildren()
	}

	@Test
	fun expiringDefaultCache() = runTest {
		val cache = adapter()
			.expireAfter(1.seconds, coroutineContext)

		testCache(cache)

		currentCoroutineContext().cancelChildren()
	}

	@Test
	fun expiringMemoryCache() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext)
			.expireAfter(1.seconds, coroutineContext)

		testCache(cache)

		currentCoroutineContext().cancelChildren()
	}

	@Test
	fun expiringMemoryCacheUpdateExpire() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext)
			.expireAfter(1.seconds, coroutineContext)

		testUpdateExpire(cache)

		currentCoroutineContext().cancelChildren()
	}

	@Test
	fun expiringMemoryCacheExpirationLayer() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext)
			.expireAfter(1.seconds, coroutineContext)

		testAutoExpiration(cache)

		currentCoroutineContext().cancelChildren()
	}

	@Test
	fun expireAll() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext)
			.expireAfter(1.seconds, coroutineContext)

		log.info { "Initial values" }
		val id0 = IntId(0)
		val id1 = IntId(1)
		assertEquals(0, cache[id0].firstValue().orThrow())
		assertEquals(1, cache[id1].firstValue().orThrow())

		log.info { "Adding 5" }
		cache.update(
			id0 to 5,
			id1 to 6,
		)
		assertEquals(5, cache[id0].firstValue().orThrow())
		assertEquals(6, cache[id1].firstValue().orThrow())

		log.info { "Expiring all values" }
		cache.expireAll()
		assertEquals(0, cache[id0].firstValue().orThrow())
		assertEquals(1, cache[id1].firstValue().orThrow())

		currentCoroutineContext().cancelChildren()
	}

	@Test
	fun batching() = runTest {
		val cache = batchingCache<IntId, Int>(coroutineContext) { ids ->
			for (ref in ids) {
				emit(ref to ProgressiveOutcome.Empty(loading()))
				delay(10)
				emit(ref to ProgressiveOutcome.Success(ref.id))
			}
		}
			.cachedInMemory(coroutineContext)
			.expireAfter(1.seconds, coroutineContext)

		log.info { "Initial values" }
		val id0 = IntId(0)
		val id1 = IntId(1)
		assertEquals(0, cache[id0].firstValue().orThrow())
		assertEquals(1, cache[id1].firstValue().orThrow())

		log.info { "Adding 5" }
		cache.update(
			id0 to 5,
			id1 to 6,
		)
		assertEquals(5, cache[id0].firstValue().orThrow())
		assertEquals(6, cache[id1].firstValue().orThrow())

		log.info { "Expiring all values" }
		cache.expireAll()
		assertEquals(0, cache[id0].firstValue().orThrow())
		assertEquals(1, cache[id1].firstValue().orThrow())

		currentCoroutineContext().cancelChildren()
	}

	@Test
	fun concurrent() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext)

		var result: ProgressiveOutcome<Int> = ProgressiveOutcome.Empty()

		log.info { "Subscribing???" }
		launch {
			cache[IntId(1)]
				.collect { result = it }
		}
		// Wait for the first cache read to finish
		delay(1000)
		while (result is ProgressiveOutcome.Empty) {
			yield()
		}
		assertEquals(ProgressiveOutcome.Success(1), result)

		log.info { "Forcing an update with an incorrect value" }
		cache.update(IntId(1), 5)
		// Wait for the cache to update
		while (result == ProgressiveOutcome.Success(1) || result.progress !is Progression.Done) {
			yield()
		}
		assertEquals(ProgressiveOutcome.Success(5), result)

		log.info { "Expiring the value to see the cache fix itself" }
		cache.expire(IntId(1))
		// Wait for the cache to update
		while (result == ProgressiveOutcome.Success(5) || result.progress !is Progression.Done) {
			yield()
		}
		assertEquals(ProgressiveOutcome.Success(1), result)

		currentCoroutineContext().cancelChildren()
	}

}
