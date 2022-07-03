package opensavvy.backbone.cache

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import opensavvy.backbone.*
import opensavvy.backbone.Ref.Companion.requestValue
import opensavvy.backbone.cache.ExpirationCache.Companion.expireAfter
import opensavvy.backbone.cache.MemoryCache.Companion.cachedInMemory
import opensavvy.logger.Logger.Companion.info
import opensavvy.logger.loggerFor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class MemoryCacheTest {

	private data class AbsoluteIntRef(val value: Int, override val backbone: Backbone<UInt>) : Ref<UInt> {
		override fun toString() = "AbsoluteIntRef($value)"
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

	private suspend fun testCache(cache: Cache<UInt>) {
		val backbone = AbsoluteIntBackbone(cache)

		val zero = backbone.convert(0)
		val one = backbone.convert(1)
		val minus = backbone.convert(-1)

		assertEquals(0u, zero.requestValue())
		assertEquals(1u, one.requestValue())
		assertEquals(1u, minus.requestValue())
	}

	@Test
	fun withoutCache() = runTest {
		testCache(Cache.Default())
	}

	@Test
	fun infiniteMemoryCache() = runTest {
		val job = Job()

		testCache(Cache.Default<UInt>()
			           .cachedInMemory(coroutineContext + job))

		job.cancel()
	}

	@Test
	fun expiringDefaultCache() = runTest {
		val job = Job()

		testCache(Cache.Default<UInt>()
			          .expireAfter(1.minutes, coroutineContext + job))

		job.cancel()
	}

	@Test
	fun expiringMemoryCache() = runTest {
		val job = Job()

		testCache(Cache.Default<UInt>()
			          .cachedInMemory(coroutineContext + job)
			          .expireAfter(1.minutes, coroutineContext + job))

		job.cancel()
	}
}
