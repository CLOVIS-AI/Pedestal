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

import arrow.core.raise.ensure
import kotlinx.coroutines.delay
import opensavvy.cache.Cache
import opensavvy.cache.cache
import opensavvy.prepared.suite.TestDsl
import opensavvy.progress.coroutines.report
import opensavvy.progress.loading
import opensavvy.state.arrow.out

sealed interface Failures {
	data class Negative(val id: Int) : Failures
}

val testIntCache = cache<Int, Failures, String> {
	out {
		delay(100)
		report(loading(0.2))
		delay(10)
		ensure(it >= 0) { Failures.Negative(it) }
		it.toString()
	}
}

typealias TestIntCacheDecorator = suspend TestDsl.(Cache<Int, Failures, String>) -> Cache<Int, Failures, String>
