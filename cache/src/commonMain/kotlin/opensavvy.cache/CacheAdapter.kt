package opensavvy.cache

import opensavvy.state.Identifier
import opensavvy.state.State

/**
 * Cache implementation aimed to be the first link in a cache chain.
 *
 * This is not a valid implementation of a cache (it doesn't do any caching), and only serves as a link between caches
 * and the underlying network APIs.
 */
class CacheAdapter<I : Identifier, T>(
	val query: (I) -> State<T>,
) : Cache<I, T> {
	override fun get(id: I) = query(id)

	override suspend fun update(values: Collection<Pair<I, T>>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expire(ids: Collection<I>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expireAll() {
		// This cache layer has no state, nothing to do
	}
}
