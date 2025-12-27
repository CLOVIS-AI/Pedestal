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

package opensavvy.cache.properties

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.job
import opensavvy.cache.batchingCache
import opensavvy.cache.cachedInMemory
import opensavvy.cache.expireAfter
import opensavvy.prepared.runner.testballoon.preparedSuite
import opensavvy.prepared.suite.backgroundScope
import opensavvy.prepared.suite.clock
import opensavvy.prepared.suite.time
import opensavvy.progress.loading
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.valueOrNull
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalCoroutinesApi::class, ExperimentalTime::class)
val BatchingCacheAdapter by preparedSuite {

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

}
