@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.cache

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import opensavvy.cache.ExpirationCache.Companion.expireAfter
import opensavvy.cache.MemoryCache.Companion.cachedInMemory
import opensavvy.logger.LogLevel
import opensavvy.logger.Logger.Companion.debug
import opensavvy.logger.Logger.Companion.info
import opensavvy.logger.loggerFor
import opensavvy.state.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class CacheTest {

	private val log = loggerFor(this).apply {
		level = LogLevel.TRACE
	}

	private data class IntId(val id: Int) : Identifier<Int> {
		override fun toString() = "Id($id)"
	}

	private fun adapter() = CacheAdapter<IntId, Int> {
		state {
			log.debug(it) { "Requesting" }
			delay(100L)
			emitPending(it, 0.2)
			delay(10L)
			ensureValid(it, it.id >= 0) { "Only positive integers are allowed: found ${it.id}" }
			emitSuccessful(it, it.id)
		}
	}

	private suspend fun testCache(cache: Cache<IntId, Int>) {
		log.info { "Regular access" }
		val zero = cache[IntId(0)]
		val one = cache[IntId(1)]
		val minus = cache[IntId(-1)]

		assertEquals(0, zero.firstResultOrNull())
		assertEquals(1, one.firstResultOrNull())
		assertEquals(null, minus.firstResultOrNull())
	}

	private suspend fun testUpdateExpire(cache: Cache<IntId, Int>) {
		log.info { "Checking normal behavior" }
		assertEquals(0, cache[IntId(0)].firstResultOrThrow())

		log.info { "Overwriting with a different value" }
		cache.update(IntId(0), 5)
		assertEquals(5, cache[IntId(0)].firstResultOrThrow())

		log.info { "Expiring the value re-downloads and replaces our fake value" }
		cache.expire(IntId(0))
		assertEquals(0, cache[IntId(0)].firstResultOrThrow())
	}

	private suspend fun testAutoExpiration(cache: Cache<IntId, Int>) {
		log.info { "Adding 5 to the cache to make updates visible" }
		assertEquals(0, cache[IntId(0)].firstResultOrThrow())
		cache.update(IntId(0), 5)
		assertEquals(5, cache[IntId(0)].firstResultOrThrow())

		log.info { "Waiting for the cache to correct itself" }
		assertEquals(0,
		             cache[IntId(0)]
			             .onEach { log.debug(it) { "Found new value" } }
			             .drop(1) // Skip the bad value we inserted
			             .firstResultOrThrow()
		)
	}

	@Test
	fun rawAdapter() = runTest {
		val cache = adapter()

		testCache(cache)
	}

	@Test
	fun infiniteMemoryCache() = runTest {
		val job = Job()
		val cache = adapter()
			.cachedInMemory(coroutineContext + job)

		testCache(cache)

		job.cancel()
	}

	@Test
	fun infiniteMemoryCacheUpdateExpire() = runTest {
		val job = Job()
		val cache = adapter()
			.cachedInMemory(coroutineContext + job)

		testUpdateExpire(cache)

		job.cancel()
	}

	@Test
	fun expiringDefaultCache() = runTest {
		val job = Job()
		val cache = adapter()
			.expireAfter(1.seconds, coroutineContext + job)

		testCache(cache)

		job.cancel()
	}

	@Test
	fun expiringMemoryCache() = runTest {
		val job = Job()
		val cache = adapter()
			.cachedInMemory(coroutineContext + job)
			.expireAfter(1.seconds, coroutineContext + job)

		testCache(cache)

		job.cancel()
	}

	@Test
	fun expiringMemoryCacheUpdateExpire() = runTest {
		val job = Job()
		val cache = adapter()
			.cachedInMemory(coroutineContext + job)
			.expireAfter(1.seconds, coroutineContext + job)

		testUpdateExpire(cache)

		job.cancel()
	}

	@Test
	fun expiringMemoryCacheExpirationLayer() = runTest {
		val job = Job()
		val cache = adapter()
			.cachedInMemory(coroutineContext + job)
			.expireAfter(1.seconds, coroutineContext + job)

		testAutoExpiration(cache)

		job.cancel()
	}

	@Test
	fun expireAll() = runTest {
		val job = Job()
		val cache = adapter()
			.cachedInMemory(coroutineContext + job)
			.expireAfter(1.seconds, coroutineContext + job)

		log.info { "Initial values" }
		val id0 = IntId(0)
		val id1 = IntId(1)
		assertEquals(0, cache[id0].firstResultOrThrow())
		assertEquals(1, cache[id1].firstResultOrThrow())

		log.info { "Adding 5" }
		cache.update(
			id0 to 5,
			id1 to 6,
		)
		assertEquals(5, cache[id0].firstResultOrThrow())
		assertEquals(6, cache[id1].firstResultOrThrow())

		log.info { "Expiring all values" }
		cache.expireAll()
		assertEquals(0, cache[id0].firstResultOrThrow())
		assertEquals(1, cache[id1].firstResultOrThrow())

		job.cancel()
	}

	@Test
	fun batching() = runTest {
		val job = Job()
		val cache = BatchingCacheAdapter<IntId, Int>(coroutineContext + job) { ids ->
			state {
				for (id in ids) {
					emitPending(id)
					delay(10L)
					emitSuccessful(id, id.id)
				}
			}
		}
			.cachedInMemory(coroutineContext + job)
			.expireAfter(1.seconds, coroutineContext + job)

		log.info { "Initial values" }
		val id0 = IntId(0)
		val id1 = IntId(1)
		assertEquals(0, cache[id0].firstResultOrThrow())
		assertEquals(1, cache[id1].firstResultOrThrow())

		log.info { "Adding 5" }
		cache.update(
			id0 to 5,
			id1 to 6,
		)
		assertEquals(5, cache[id0].firstResultOrThrow())
		assertEquals(6, cache[id1].firstResultOrThrow())

		log.info { "Expiring all values" }
		cache.expireAll()
		assertEquals(0, cache[id0].firstResultOrThrow())
		assertEquals(1, cache[id1].firstResultOrThrow())

		job.cancel()
	}

}
