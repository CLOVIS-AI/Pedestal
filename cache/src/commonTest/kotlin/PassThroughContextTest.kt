package opensavvy.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import opensavvy.cache.MemoryCache.Companion.cachedInMemory
import opensavvy.state.failure.Failure
import opensavvy.state.outcome.Outcome
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class PassThroughContextTest {

    class CustomContext : AbstractCoroutineContextElement(Companion) {
        companion object : CoroutineContext.Key<CustomContext>
    }

    @Test
    fun memoryCache() = runTest {
        val cache = cache<Boolean, Failure, Unit> {
            println(currentCoroutineContext())
            if (it) {
                assertNotNull(currentCoroutineContext()[CustomContext])
            } else {
                assertNull(currentCoroutineContext()[CustomContext])
            }
            Outcome.Success(Unit)
        }.cachedInMemory(backgroundScope.coroutineContext.job)

        withContext(CustomContext() + CoroutineName("The context should be present")) {
            cache[true]
        }
        withContext(CoroutineName("The context should not be present")) {
            cache[false]
        }
    }
}
