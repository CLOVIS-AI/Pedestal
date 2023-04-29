package opensavvy.cache.contextual

import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.coroutines.captureProgress
import opensavvy.state.outcome.Outcome

/**
 * Cache implementation aimed to be the first link in a cache chain.
 *
 * This is not a valid implementation of a cache (it doesn't do any caching), and only serves as a link
 * between caches and the underlying network APIs.
 */
class ContextualCacheAdapter<I, C, F, T>(
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
 * Creates a cache layer that intercepts requests.
 *
 * See [ContextualCacheAdapter].
 */
fun <I, C, F, T> cache(transform: suspend (I, C) -> Outcome<F, T>): ContextualCache<I, C, F, T> =
	ContextualCacheAdapter(transform)
