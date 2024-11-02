package opensavvy.cache

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.job
import kotlinx.coroutines.withTimeout
import opensavvy.cache.properties.TestIntCacheDecorator
import opensavvy.cache.properties.readingValues
import opensavvy.cache.properties.testIntCache
import opensavvy.cache.properties.updateAndExpire
import opensavvy.prepared.compat.kotlinx.datetime.clock
import opensavvy.prepared.compat.kotlinx.datetime.now
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.*
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

private val expirationDuration = 10.seconds

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class ExpirationCacheTest : PreparedSpec({

	fun <A, B, C> TestDsl.decorateExpiration(upstream: Cache<A, B, C>): Cache<A, B, C> = upstream
		.expireAfter(expirationDuration, backgroundScope, time.clock)

	fun <A, B, C> TestDsl.decorateMemoryAndExpiration(upstream: Cache<A, B, C>): Cache<A, B, C> = upstream
		.cachedInMemory(backgroundScope.coroutineContext.job)
		.expireAfter(expirationDuration, backgroundScope, time.clock)

	suite("Expiration only") {
		readingValues { decorateExpiration(it) }
	}

	suite("Cached in-memory and expiration") {
		readingValues { decorateMemoryAndExpiration(it) }
		updateAndExpire { decorateMemoryAndExpiration(it) }
		automaticallyExpires { decorateMemoryAndExpiration(it) }
	}
})

@OptIn(ExperimentalTime::class, ExperimentalCoroutinesApi::class)
private fun SuiteDsl.automaticallyExpires(
	cacheWrapper: TestIntCacheDecorator,
) = suite("The expiration cache should expire itself") {
	val testCache by prepared {
		cacheWrapper(testIntCache)
	}

	test("Querying a value after the expiration threshold should lead to an updated value") {
		val cache = testCache()

		println("Inserting an incorrect value (to be able to notice when the cache will refresh itself)")
		cache.update(2, "3")
		check("3" == cache[2].now().valueOrNull)

		println("${time.now} Waiting for the cache to expire…")
		delay(expirationDuration * 2) // The exact time of expiration is not guaranteed

		println("${time.now} Checking that the cache is querying a new value…")
		check("2" == cache[2].now().valueOrNull)
	}

	test("Existing cache subscribers should be notified when the value expires") {
		val cache = testCache()

		println("Inserting an incorrect value (to be able to notice when the cache will refresh itself)")
		cache.update(2, "3")
		check("3" == cache[2].now().valueOrNull)

		println("Subscribing to the value…")
		withTimeout(expirationDuration * 2) {
			check("2" == cache[2].drop(1).now().valueOrNull)
		}
	}
}
