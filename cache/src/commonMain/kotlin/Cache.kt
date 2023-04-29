package opensavvy.cache

import kotlinx.coroutines.flow.Flow
import opensavvy.state.coroutines.ProgressiveFlow

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
 * The first element of the chain, and therefore the one responsible for actually starting the request, is [CacheAdapter] or [BatchingCacheAdapter].
 * Note that both have a few implementation differences, it is not recommended to use them directly without chaining under another implementation.
 */
interface Cache<I, F, T> {

	/**
	 * Gets the value associated with an [id] in this cache.
	 *
	 * This function returns a [Flow] instance synchronously: it is safe to call in synchronous-only areas of the program,
	 * such as inside the body of a UI component.
	 * You can then subscribe to the [Flow] to access the actual values.
	 */
	operator fun get(id: I): ProgressiveFlow<F, T>

	/**
	 * Forces the cache to accept [value] as a more recent value for the given [id] than whatever it was previously storing.
	 *
	 * All layers are updated.
	 */
	suspend fun update(id: I, value: T) {
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
	suspend fun update(values: Collection<Pair<I, T>>)

	/**
	 * Forces the cache to accept the given [values] as more recent for their associated identifier than whatever was
	 * previously stored.
	 *
	 * This overload is provided for convenience, see [update].
	 */
	suspend fun update(vararg values: Pair<I, T>) = update(values.asList())

	/**
	 * Tells the cache that the value it stores for [id] is out of date, and should be queried again the next time it is
	 * requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expire(id: I) {
		expire(listOf(id))
	}

	/**
	 * Tells the cache that the value it stores for the given [ids] are out of date, and should be queried again the next
	 * time they are requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expire(ids: Collection<I>)

	/**
	 * Tells the cache that all values are out of date, and should be queried again the next time they are requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expireAll()

}
