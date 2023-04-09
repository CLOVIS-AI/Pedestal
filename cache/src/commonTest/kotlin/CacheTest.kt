@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.cache

import arrow.core.raise.ensure
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import opensavvy.cache.BatchingCacheAdapter.Companion.batchingCache
import opensavvy.cache.CacheAdapter.Companion.cache
import opensavvy.cache.ExpirationCache.Companion.expireAfter
import opensavvy.cache.MemoryCache.Companion.cachedInMemory
import opensavvy.logger.LogLevel
import opensavvy.logger.Logger.Companion.debug
import opensavvy.logger.Logger.Companion.info
import opensavvy.logger.loggerFor
import opensavvy.progress.Progress
import opensavvy.progress.coroutines.report
import opensavvy.progress.loading
import opensavvy.state.arrow.out
import opensavvy.state.coroutines.firstValue
import opensavvy.state.failure.CustomFailure
import opensavvy.state.failure.Failure
import opensavvy.state.outcome.valueOrNull
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class CacheTest {

	private val log = loggerFor(this).apply {
		level = LogLevel.TRACE
	}

	private data class IntId(val id: Int) {
		override fun toString() = "Id($id)"

		sealed interface Failures : Failure {
			data class Negative(val id: Int) :
				CustomFailure(Companion, "Only positive integers are allowed, found $id"),
				Failures {
				companion object : Failure.Key {
					override fun toString() = "Negative"
				}
			}
		}
	}

	private fun adapter() = cache<IntId, IntId.Failures, Int> {
		out {
			log.debug(it) { "Requesting" }
			delay(100)
			report(loading(0.2))
			delay(10)
			ensure(it.id >= 0) { IntId.Failures.Negative(it.id) }
			it.id
		}
	}

	private suspend fun testCache(cache: Cache<IntId, IntId.Failures, Int>) {
		log.info { "Regular access" }
		val zero = cache[IntId(0)]
		val one = cache[IntId(1)]
		val minus = cache[IntId(-1)]

		assertEquals(0, zero.firstValue().valueOrNull)
		assertEquals(1, one.firstValue().valueOrNull)
		assertEquals(null, minus.firstValue().valueOrNull)
	}

	private suspend fun testUpdateExpire(cache: Cache<IntId, IntId.Failures, Int>) {
		log.info { "Checking normal behavior" }
		assertEquals(0, cache[IntId(0)].firstValue().valueOrNull)

		log.info { "Overwriting with a different value" }
		cache.update(IntId(0), 5)
		assertEquals(5, cache[IntId(0)].firstValue().valueOrNull)

		log.info { "Expiring the value re-downloads and replaces our fake value" }
		cache.expire(IntId(0))
		assertEquals(0, cache[IntId(0)].firstValue().valueOrNull)
	}

	private suspend fun testAutoExpiration(cache: Cache<IntId, IntId.Failures, Int>) {
		log.info { "Adding 5 to the cache to make updates visible" }
		assertEquals(0, cache[IntId(0)].firstValue().valueOrNull)
		cache.update(IntId(0), 5)
		assertEquals(5, cache[IntId(0)].firstValue().valueOrNull)

		log.info { "Waiting for the cache to correct itself" }
		assertEquals(0,
			cache[IntId(0)]
				.onEach { log.debug(it) { "Found new value" } }
				.drop(1) // Skip the bad value we inserted
				.firstValue()
				.valueOrNull
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
			.cachedInMemory(coroutineContext.job)

		testCache(cache)
	}

	@Test
	fun infiniteMemoryCacheUpdateExpire() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext.job)

		testUpdateExpire(cache)

		currentCoroutineContext().cancelChildren()
	}

	@Test
	fun expiringDefaultCache() = runTest {
		val cache = adapter()
			.expireAfter(1.seconds, backgroundScope)

		testCache(cache)
	}

	@Test
	fun expiringMemoryCache() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext.job)
			.expireAfter(1.seconds, backgroundScope)

		testCache(cache)
	}

	@Test
	fun expiringMemoryCacheUpdateExpire() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext.job)
			.expireAfter(1.seconds, backgroundScope)

		testUpdateExpire(cache)
	}

	@Test
	fun expiringMemoryCacheExpirationLayer() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext.job)
			.expireAfter(1.seconds, backgroundScope)

		testAutoExpiration(cache)
	}

	@Test
	fun expireAll() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext.job)
			.expireAfter(1.seconds, backgroundScope)

		log.info { "Initial values" }
		val id0 = IntId(0)
		val id1 = IntId(1)
		assertEquals(0, cache[id0].firstValue().valueOrNull)
		assertEquals(1, cache[id1].firstValue().valueOrNull)

		log.info { "Adding 5" }
		cache.update(
			id0 to 5,
			id1 to 6,
		)
		assertEquals(5, cache[id0].firstValue().valueOrNull)
		assertEquals(6, cache[id1].firstValue().valueOrNull)

		log.info { "Expiring all values" }
		cache.expireAll()
		assertEquals(0, cache[id0].firstValue().valueOrNull)
		assertEquals(1, cache[id1].firstValue().valueOrNull)
	}

	@Test
	fun batching() = runTest {
		val cache = batchingCache<IntId, IntId.Failures, Int>(backgroundScope) { ids ->
			for (ref in ids) {
				emit(ref to ProgressiveOutcome.Incomplete(loading()))
				delay(10)
				emit(ref to ProgressiveOutcome.Success(ref.id))
			}
		}
			.cachedInMemory(coroutineContext.job)
			.expireAfter(1.seconds, backgroundScope)

		log.info { "Initial values" }
		val id0 = IntId(0)
		val id1 = IntId(1)
		assertEquals(0, cache[id0].firstValue().valueOrNull)
		assertEquals(1, cache[id1].firstValue().valueOrNull)

		log.info { "Adding 5" }
		cache.update(
			id0 to 5,
			id1 to 6,
		)
		assertEquals(5, cache[id0].firstValue().valueOrNull)
		assertEquals(6, cache[id1].firstValue().valueOrNull)

		log.info { "Expiring all values" }
		cache.expireAll()
		assertEquals(0, cache[id0].firstValue().valueOrNull)
		assertEquals(1, cache[id1].firstValue().valueOrNull)
	}

	@Test
	fun concurrent() = runTest {
		val cache = adapter()
			.cachedInMemory(coroutineContext.job)

		var result: ProgressiveOutcome<IntId.Failures, Int> = ProgressiveOutcome.Incomplete()

		log.info { "Subscribing…" }
		val subscriber = launch {
			cache[IntId(1)]
				.collect { result = it }
		}
		// Wait for the first cache read to finish
		delay(1000)
		while (result is ProgressiveOutcome.Incomplete) {
			yield()
		}
		assertEquals(ProgressiveOutcome.Success(1), result)

		log.info { "Forcing an update with an incorrect value" }
		cache.update(IntId(1), 5)
		// Wait for the cache to update
		while (result == ProgressiveOutcome.Success(1) || result.progress !is Progress.Done) {
			yield()
		}
		assertEquals(ProgressiveOutcome.Success(5), result)

		log.info { "Expiring the value to see the cache fix itself" }
		cache.expire(IntId(1))
		// Wait for the cache to update
		while (result == ProgressiveOutcome.Success(5) || result.progress !is Progress.Done) {
			yield()
		}
		assertEquals(ProgressiveOutcome.Success(1), result)

		subscriber.cancel()
	}

}
