package opensavvy.cache

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.yield
import opensavvy.cache.properties.Failures
import opensavvy.cache.properties.readingValues
import opensavvy.cache.properties.testIntCache
import opensavvy.cache.properties.updateAndExpire
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.TestDsl
import opensavvy.prepared.suite.backgroundScope
import opensavvy.prepared.suite.launchInBackground
import opensavvy.progress.Progress
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.successfulWithProgress

class MemoryCacheTest : PreparedSpec({

	fun <A, B, C> TestDsl.decorate(upstream: Cache<A, B, C>): Cache<A, B, C> = upstream
		.cachedInMemory(backgroundScope.coroutineContext.job)

	readingValues { decorate(it) }
	updateAndExpire { decorate(it) }

	test("Concurrent scenario") {
		val cache = decorate(testIntCache)

		var result: ProgressiveOutcome<Failures, String> = ProgressiveOutcome.Incomplete()

		println("Subscribing…")
		launchInBackground(CoroutineName("Collect cache[1]")) {
			cache[1].collect { result = it }
		}

		// Wait for the first read to finish
		delay(100)
		yieldUntil { result !is ProgressiveOutcome.Incomplete }
		check(result == "1".successfulWithProgress())

		println("Forcing an update with an incorrect value")
		cache.update(1, "5")
		yieldUntil { result != ProgressiveOutcome.Success("1") && result.progress is Progress.Done }
		check(result == "5".successfulWithProgress())

		println("Expiring the value to see the cache fix itself")
		cache.expire(1)
		yieldUntil { result != ProgressiveOutcome.Success("5") && result.progress is Progress.Done }
		check(result == "1".successfulWithProgress())
	}

})

private suspend fun yieldUntil(predicate: () -> Boolean) {
	while (!predicate()) {
		delay(100)
		yield()
	}
}
