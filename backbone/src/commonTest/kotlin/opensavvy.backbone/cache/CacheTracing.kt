package opensavvy.backbone.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.test.runTest
import opensavvy.backbone.*
import opensavvy.backbone.Ref.Companion.requestValue
import opensavvy.backbone.cache.MemoryCache.Companion.cachedInMemory
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CacheTracing  {
	private data class TracingRef(val value: Int, override val backbone: Backbone<Int>) : Ref<Int> {
		override fun toString() = "Int($value)"
	}

	private class TracingBackbone(override val cache: Cache<Int>): Backbone<Int> {
		var tracker = 0
		val lock = Semaphore(1)

		override fun directRequest(ref: Ref<Int>): Flow<Data<Int>> = flow {
			require(ref is TracingRef) { "This backbone only works with TracingRef, found $ref" }

			lock.withPermit {
				tracker++
			}

			delay(10) // give time to the other coroutines to execute
			emit(Data(Result.Success(ref.value), Data.Status.Completed, ref))
		}

		fun get(value: Int) = TracingRef(value, this)
	}

	/**
	 * Two different subscribers on the same ref
	 *
	 * If we expire this ref, only one real request must be launched (not one each!)
	 */
	@Test
	fun concurrentRequest() = runTest {
		val job = Job()

		val backbone = TracingBackbone(Cache.Default<Int>().cachedInMemory(coroutineContext + job))
		val zero = backbone.get(0)
		assertEquals(0, backbone.tracker)

		coroutineScope {
			// Start 10 parallel requests
			repeat(10) {
				launch {
					assertEquals(0, zero.requestValue())
				}
			}
		}

		// After the 10 requests have finished, check that only one real request was sent
		assertEquals(1, backbone.tracker)

		job.cancel()
	}
}
