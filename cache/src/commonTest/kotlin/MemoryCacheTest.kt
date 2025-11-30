/*
 * Copyright (c) 2024-2025, OpenSavvy and contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package opensavvy.cache

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import kotlinx.coroutines.yield
import opensavvy.cache.properties.*
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.TestDsl
import opensavvy.prepared.suite.backgroundScope
import opensavvy.prepared.suite.launchInBackground
import opensavvy.progress.Progress
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.successfulWithProgress

val MemoryCacheTest by preparedSuite {

	fun <A, B, C> TestDsl.decorate(upstream: Cache<A, B, C>): Cache<A, B, C> = upstream
		.cachedInMemory(backgroundScope.coroutineContext.job)

	readingValues { decorate(it) }
	updateAndExpire { decorate(it) }
	contextPassthrough { decorate(it) }

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

}

private suspend fun yieldUntil(predicate: () -> Boolean) {
	while (!predicate()) {
		delay(100)
		yield()
	}
}
