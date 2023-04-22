package opensavvy.cache

import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.coroutines.captureProgress
import opensavvy.state.failure.Failure
import opensavvy.state.outcome.Outcome

/**
 * Cache implementation aimed to be the first link in a cache chain.
 *
 * This is not a valid implementation of a cache (it doesn't do any caching), and only serves as a link between caches
 * and the underlying network APIs.
 */
class CacheAdapter<I, F : Failure, T>(
	val query: suspend (I) -> Outcome<F, T>,
) : Cache<I, F, T> {

	override fun get(id: I): ProgressiveFlow<F, T> = captureProgress { query(id) }

	override suspend fun update(values: Collection<Pair<I, T>>) {
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
 * Creates a cache layer that intercepts requests.
 *
 * See [CacheAdapter].
 */
fun <I, F : Failure, T> cache(transform: suspend (I) -> Outcome<F, T>) =
	CacheAdapter(transform)
