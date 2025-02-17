/*
 * Copyright (c) 2022-2025, OpenSavvy and contributors.
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

import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.coroutines.captureProgress
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.successful
import kotlin.jvm.JvmName

internal class CacheAdapter<I, F, V>(
	private val query: suspend (I) -> Outcome<F, V>,
) : Cache<I, F, V> {

	override fun get(id: I): ProgressiveFlow<F, V> = captureProgress { query(id) }

	override suspend fun update(values: Collection<Pair<I, V>>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expire(ids: Collection<I>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expireAll() {
		// This cache layer has no state, nothing to do
	}

	companion object
}

/**
 * Cache implementation which calls a given [transform] suspending function.
 *
 * This adapter is meant to be used as the first layer in a layer chain.
 * By itself, it does no caching (all calls to [get][Cache.get] call [transform]).
 * To learn more about layer chaining, or about the type parameters, see [Cache].
 *
 * ### Example
 *
 * ```kotlin
 * object NegativeNumber
 *
 * val squaredRoot = cache<Double, NegativeNumber, Double> {
 *     if (it >= 0) {
 *         sqrt(it).success()
 *     } else {
 *         NegativeInteger.failed()
 *     }
 * }
 *
 * println(squaredRoot[25.0].now()) // Success(5.0)
 * println(squaredRoot[-5.0].now()) // Failure(NegativeNumber)
 * ```
 */
fun <Identifier, Failure, Value> cache(transform: suspend (Identifier) -> Outcome<Failure, Value>): Cache<Identifier, Failure, Value> =
	CacheAdapter(transform)

/**
 * Cache implementation which calls a given [transform] suspending function.
 *
 * The [transform] function is considered always successful.
 * This allows to bypass the cache's error encoding.
 * For more information, see [InfallibleCache].
 *
 * This adapter is meant to be used as the first layer in a layer chain.
 * By itself, it does no caching (all calls to [get][Cache.get] call [transform]).
 * To learn more about layer chaining, or about the type parameters, see [Cache].
 *
 * ### Example
 *
 * ```kotlin
 * val squared = cache<Int, Int> { it * 2 }
 *
 * println(squared[5]) // Success(25)
 * ```
 */
@JvmName("infallibleCache")
fun <Identifier, Value> cache(transform: suspend (Identifier) -> Value): InfallibleCache<Identifier, Value> =
	CacheAdapter { transform(it).successful() }
