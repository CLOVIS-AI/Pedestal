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

import kotlinx.coroutines.flow.Flow
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.ProgressiveOutcome

/**
 * Stores information temporarily to avoid unneeded network requests.
 *
 * ### Cache state
 *
 * Each piece of data in a cache can be in three different states:
 * - Up-to-date: the last query was not long ago, we consider its result still valid,
 * - Stale: the last query was long enough ago that it deserves to be checked again, but it probably is still valid.
 * In this case, the cache returns the old value and starts a refresh request in the background.
 * - Expired: the last query was too long ago for the data to still be valid.
 * In this case, the cache starts a refresh request and will not return a value until the request finishes.
 * All data that was never previously queried is in this state.
 *
 * Different cache implementations differ on how they transition data from one state to another.
 * Some cache implementations may not have a 'stale' state.
 * As a user of the cache, you may want to force the state of a specific object if you have more knowledge than the cache,
 * in this case you can use [update] and [expire].
 *
 * ### Cache chaining
 *
 * Cache implementations can be chained.
 * A possible scenario for some data that rarely changes can be:
 * - Cache the data in memory for 5 minutes,
 * - Cache the data in hard storage for 1 hour,
 * - Query the data for real afterward.
 *
 * Cache chaining is instantiated in the opposite order, like iterators (the last in the chain is the first checked,
 * and delegates to the previous one if they do not have the value).
 * The first element of the chain, and therefore the one responsible for actually starting the request, is [cache] or [batchingCache].
 * Note that both have a few implementation differences, it is not recommended to use them directly without chaining under another implementation.
 *
 * @param Identifier An identifier representing a cached object. Two identifiers refer to the same object if their [equals][Any.equals] method returns `true`.
 * @param Failure The type of possible failures which may happen when requesting a value. If the cached operation cannot fail, or if you're using another error-handling strategy, see [InfallibleCache].
 * @param Value The type of cached object.
 */
interface Cache<Identifier, Failure, Value> {

	/**
	 * Gets the value associated with an [id] in this cache.
	 *
	 * This function returns a [Flow] instance synchronously: it is safe to call in synchronous-only areas of the program,
	 * such as inside the body of a UI component.
	 * You can then subscribe to the [Flow] to access the actual values.
	 */
	operator fun get(id: Identifier): ProgressiveFlow<Failure, Value>

	/**
	 * Forces the cache to accept [value] as a more recent value for the given [id] than whatever it was previously storing.
	 *
	 * All layers are updated.
	 */
	suspend fun update(id: Identifier, value: Value) {
		update(listOf(id to value))
	}

	/**
	 * Forces the cache to accept the given [values] as more recent for their associated identifier than whatever was previously stored.
	 *
	 * If multiple values are provided for the same identifier, a cache implementation may either:
	 * - take only the last occurrence into account,
	 * - take each occurrence into account as subsequent updates, in the same order as they appear in the iterable.
	 *
	 * If [values] is empty, this function does nothing.
	 *
	 * All layers are updated.
	 */
	suspend fun update(values: Collection<Pair<Identifier, Value>>)

	/**
	 * Forces the cache to accept the given [values] as more recent for their associated identifier than whatever was
	 * previously stored.
	 *
	 * This overload is provided for convenience, see [update].
	 */
	suspend fun update(vararg values: Pair<Identifier, Value>) = update(values.asList())

	/**
	 * Tells the cache that the value it stores for [id] is out of date, and should be queried again the next time it is
	 * requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expire(id: Identifier) {
		expire(listOf(id))
	}

	/**
	 * Tells the cache that the value it stores for the given [ids] are out of date, and should be queried again the next
	 * time they are requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expire(ids: Collection<Identifier>)

	/**
	 * Tells the cache that all values are out of date, and should be queried again the next time they are requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expireAll()

}

/**
 * Stores information temporarily to avoid unneeded network requests, with no error strategy.
 *
 * By default, [Cache] represents errors as an alternative object, using [ProgressiveOutcome].
 * When caching an operation which cannot fail, or an operation which uses some other kind of
 * error handling strategy (exceptions, custom sealed classes…) it can be convenient to avoid
 * Cache's default error encoding completely.
 *
 * This type alias represents the opting-out of Cache's default error encoding.
 */
typealias InfallibleCache<Identifier, Value> = Cache<Identifier, Nothing, Value>
