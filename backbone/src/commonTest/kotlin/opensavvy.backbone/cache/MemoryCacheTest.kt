package opensavvy.backbone.cache

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.runTest
import opensavvy.backbone.*
import opensavvy.backbone.Data.Companion.firstSuccessOrThrow
import opensavvy.backbone.Data.Companion.skipLoading
import opensavvy.backbone.Ref.Companion.expire
import opensavvy.backbone.Ref.Companion.request
import opensavvy.backbone.Ref.Companion.requestValue
import opensavvy.backbone.cache.ExpirationCache.Companion.expireAfter
import opensavvy.backbone.cache.MemoryCache.Companion.cachedInMemory
import opensavvy.logger.Logger.Companion.info
import opensavvy.logger.loggerFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryCacheTest {

	private data class AbsoluteIntRef(val value: Int, override val backbone: Backbone<UInt>) : Ref<UInt> {
		override fun toString() = "UIntOf($value)"
	}

	private class AbsoluteIntBackbone(override val cache: Cache<UInt>) : Backbone<UInt> {
		override fun directRequest(ref: Ref<UInt>): Flow<Data<UInt>> = flow {
			require(ref is AbsoluteIntRef)

			log.info { "directRequest called for $ref" }

			if (ref.value > 0)
				emit(Data(Result.Success(ref.value.toUInt()), Data.Status.Completed, ref))
			else
				emit(Data(Result.Success((-ref.value).toUInt()), Data.Status.Completed, ref))
		}

		fun convert(value: Int) = AbsoluteIntRef(value, this)

		companion object {
			private val log = loggerFor(this)
		}
	}

	/**
	 * Tests that running a request through the cache actually sends it to the backbone
	 */
	private suspend fun testCache(cache: Cache<UInt>) {
		cache.expireAllRecursively()
		val backbone = AbsoluteIntBackbone(cache)

		println("Normal access via directRequest...")
		val zero = backbone.convert(0)
		val one = backbone.convert(1)
		val minus = backbone.convert(-1)

		assertEquals(0u, zero.requestValue())
		assertEquals(1u, one.requestValue())
		assertEquals(1u, minus.requestValue())
	}

	/**
	 * Tests updating and expiring a value
	 *
	 * This test only applies to stateful cache layers (e.g. MemoryCache)
	 */
	private suspend fun testUpdateExpiration(cache: Cache<UInt>) {
		cache.expireAllRecursively()
		val backbone = AbsoluteIntBackbone(cache)

		println("\nForcing a different value")
		val zero = backbone.convert(0)
		backbone.cache.update(zero, 5u)
		assertEquals(5u, zero.requestValue())

		println("\nExpiring the value re-downloads it and replaces our invalid value")
		zero.expire()
		assertEquals(0u, zero.requestValue())
	}

	/**
	 * Tests that the data is correctly expired
	 *
	 * This test only applies to layers which automatically expire data after some time (e.g. ExpirationCache)
	 */
	private suspend fun testAutoExpiration(cache: Cache<UInt>) {
		cache.expireAllRecursively()
		val backbone = AbsoluteIntBackbone(cache)

		println("\nAdding 5u to the cache to make updates visible")
		val zero = backbone.convert(0)
		assertEquals(0u, zero.requestValue())
		backbone.cache.update(
			zero,
			5u
		) // adding a weird value, so we can detect when it decides to automatically trigger the update

		println("Querying until the cache a non-5u value")
		assertEquals(0u, zero.request()
			.skipLoading()
			.onEach { println("-> $it") }
			.drop(1) // ignore the 5u
			.firstSuccessOrThrow())
	}

	@Test
	fun withoutCache() = runTest {
		val cache = Cache.Default<UInt>()

		testCache(cache)
	}

	@Test
	fun infiniteMemoryCache() = runTest {
		val job = Job()
		val cache = Cache.Default<UInt>()
			.cachedInMemory(coroutineContext + job)

		testCache(cache)
		testUpdateExpiration(cache)

		job.cancel()
	}

	@Test
	fun expiringDefaultCache() = runTest {
		val job = Job()
		val cache = Cache.Default<UInt>()
			.expireAfter(1.minutes, coroutineContext + job)

		testCache(cache)

		job.cancel()
	}

	@Test
	fun expiringMemoryCache() = runTest {
		val job = Job()
		val cache = Cache.Default<UInt>()
			.cachedInMemory(coroutineContext + job)
			.expireAfter(300.milliseconds, coroutineContext + job)

		testCache(cache)
		testUpdateExpiration(cache)
		testAutoExpiration(cache)

		job.cancel()
	}
}
