package opensavvy.cache

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import opensavvy.cache.CacheAdapter.Companion.cache
import opensavvy.cache.ExpirationCache.Companion.expireAfter
import opensavvy.cache.MemoryCache.Companion.cachedInMemory
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.minutes

class ShouldNotPassThrough : AbstractCoroutineContextElement(ShouldNotPassThrough) {
	companion object : CoroutineContext.Key<ShouldNotPassThrough>
}

class ShouldPassThrough : AbstractCoroutineContextElement(ShouldPassThrough), PassThroughContext {
	companion object : CoroutineContext.Key<ShouldPassThrough>
}

@OptIn(ExperimentalCoroutinesApi::class)
class PassThroughTest {

	@Test
	fun passThroughTest() = runTest {
		val shouldNot = ShouldNotPassThrough()
		val should = ShouldPassThrough()

		val cache = cache<Unit, Unit> {
			assertEquals(null, currentCoroutineContext()[ShouldNotPassThrough])
			assertEquals(should, currentCoroutineContext()[ShouldPassThrough])
		}
			.cachedInMemory(backgroundScope.coroutineContext)
			.expireAfter(2.minutes, backgroundScope.coroutineContext)

		withContext(should + shouldNot) {
			cache[Unit].first()
		}
	}

}
