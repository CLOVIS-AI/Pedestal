package opensavvy.cache.contextual

import opensavvy.cache.Cache
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.coroutines.captureProgress
import opensavvy.state.outcome.Outcome

internal class ContextualCacheAdapter<I, C, F, T>(
	private val query: suspend (I, C) -> Outcome<F, T>,
) : ContextualCache<I, C, F, T> {
	override fun get(id: I, context: C): ProgressiveFlow<F, T> = captureProgress { query(id, context) }

	override suspend fun update(values: Collection<Triple<I, C, T>>) {
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
fun <I, C, F, T> cache(transform: suspend (I, C) -> Outcome<F, T>): ContextualCache<I, C, F, T> =
	ContextualCacheAdapter(transform)
