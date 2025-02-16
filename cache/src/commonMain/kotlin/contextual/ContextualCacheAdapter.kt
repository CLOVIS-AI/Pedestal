/*
 * Copyright (c) 2023-2025, OpenSavvy and contributors.
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

package opensavvy.cache.contextual

import opensavvy.cache.Cache
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.coroutines.captureProgress
import opensavvy.state.outcome.Outcome

internal class ContextualCacheAdapter<I, C, F, V>(
	private val query: suspend (I, C) -> Outcome<F, V>,
) : ContextualCache<I, C, F, V> {
	override fun get(id: I, context: C): ProgressiveFlow<F, V> = captureProgress { query(id, context) }

	override suspend fun update(values: Collection<Triple<I, C, V>>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expire(ids: Collection<I>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expireContextual(ids: Collection<Pair<I, C>>) {
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
 * This adapter is meant to be used as the first layer in a layer chain. By itself, it does no caching (all calls to [get][ContextualCache.get]  call [transform]).
 * To learn more about layer chaining, see [Cache].
 * To learn more about the type parameters, see [ContextualCache].
 *
 * ### Example
 *
 * ```kotlin
 * class User
 * class SearchFilters
 * data class Page(val number: Int)
 * data class Post(…)
 *
 * suspend fun getTimeline(filters: SearchFilters, user: User, page: Page): List<Post> = …
 *
 * val cachedTimeline = cache<SearchFilters, Pair<User, Page>, Nothing, List<Post>> { it, (user, page) ->
 *     getTimeline(filters, user, page).success()
 * }
 *
 * val me = User()
 * val filters = SearchFilters()
 *
 * println(cachedTimeline[filters, me to Page(0)].now())
 * println(cachedTimeline[filters, me to Page(1)].now())
 * ```
 *
 * @see opensavvy.cache.cache Non-contextual equivalent
 */
fun <Identifier, Context, Failure, Value> cache(transform: suspend (Identifier, Context) -> Outcome<Failure, Value>): ContextualCache<Identifier, Context, Failure, Value> =
	ContextualCacheAdapter(transform)
