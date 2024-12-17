package opensavvy.cache.properties

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import opensavvy.cache.batchingCache
import opensavvy.cache.cachedInMemory
import opensavvy.cache.expireAfter
import opensavvy.prepared.compat.kotlinx.datetime.clock
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.backgroundScope
import opensavvy.prepared.suite.time
import opensavvy.progress.loading
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
class BatchingCacheAdapter : PreparedSpec({

	test("Checking for batching") {
		val cache = batchingCache<Int, Failures, String>(backgroundScope) { ids ->
			for (ref in ids) {
				emit(ref to ProgressiveOutcome.Incomplete(loading()))
				delay(10)
				emit(ref to ProgressiveOutcome.Success(ref.toString()))
			}
		}
			.cachedInMemory(coroutineContext.job)
			.expireAfter(1.seconds, backgroundScope, time.clock)

		println("Initial values")
		check("0" == cache[0].now().valueOrNull)
		check("1" == cache[1].now().valueOrNull)

		println("Adding 5")
		cache.update(0 to "5", 1 to "6")
		check("5" == cache[0].now().valueOrNull)
		check("6" == cache[1].now().valueOrNull)

		println("Expiring all values")
		cache.expireAll()
		check("0" == cache[0].now().valueOrNull)
		check("1" == cache[1].now().valueOrNull)
	}

})
