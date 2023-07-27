package opensavvy.cache.blocking

import kotlinx.coroutines.runBlocking
import opensavvy.cache.Cache
import opensavvy.cache.InfallibleCache
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.value

/**
 * A blocking cache implementation.
 *
 * Unlike [asynchronous caches][Cache], this implementation doesn't allow subscribing to a value to see it change over time.
 *
 * To instantiate this class, see the [blocking] helper.
 *
 * @param I An identifier representing a cached object.
 * @param F A cache value that represents a failure.
 * @param T A cache value that represents a success.
 * @property upstream The underlying cache instance.
 */
class BlockingCache<I, F, T>(
	private val upstream: Cache<I, F, T>,
) {

	/**
	 * Gets the value associated with [id] in the cache, at the current time.
	 *
	 * Unlike [Cache.get], this function does not allow subscribing to the value to see its changes over time.
	 */
	operator fun get(id: I): Outcome<F, T> = runBlocking {
		upstream[id].now()
	}

	/**
	 * Forces the cache to accept [value] as a more recent value for the given [id] than whatever it was previously storing.
	 *
	 * For more information, see [Cache.update].
	 */
	operator fun set(id: I, value: T) = runBlocking {
		upstream.update(id, value)
	}

	/**
	 * Forces the cache to accept the given [values] as more recent than their associated identifier than whatever was
	 * previously stored.
	 *
	 * For more information, see [Cache.update].
	 */
	fun update(values: Collection<Pair<I, T>>) = runBlocking {
		upstream.update(values)
	}

	/**
	 * Forces the cache to accept the given [values] as more recent than their associated identifier than whatever was
	 * previously stored.
	 *
	 * For more information, see [Cache.update].
	 */
	fun update(vararg values: Pair<I, T>) = update(values.asList())

	/**
	 * Tells the cache that the value it stores for [id] is out-of-date, and should be queried again the next time it is requested.
	 *
	 * For more information, see [Cache.expire].
	 */
	fun expire(id: I) {
		expire(listOf(id))
	}

	/**
	 * Tells the cache that the value it stores for the given [ids] are out-of-date, and should be queried again next time they are requested.
	 *
	 * For more information, see [Cache.expire].
	 */
	fun expire(ids: Collection<I>) = runBlocking {
		upstream.expire(ids)
	}

	/**
	 * Tells the cache that all values are out-of-date, and should be queried again the next time they are requested.
	 *
	 * For more information, see [Cache.expireAll].
	 */
	fun expireAll() = runBlocking {
		upstream.expireAll()
	}
}

/**
 * Convenience function to access a value from [infallible caches][InfallibleCache] that are blocking.
 *
 * For more information, see [BlockingCache.get] and [Cache.get].
 */
fun <I, T> BlockingCache<I, Nothing, T>.getValue(id: I): T =
	get(id).value

/**
 * Converts an [asynchronous cache][Cache] into a [blocking cache][BlockingCache].
 *
 * ### Example
 *
 * ```kotlin
 * // Create the coroutine context
 * val cachingJob = SupervisorJob()
 * val cachingScope = CoroutineScope(cachingJob)
 *
 * // Instantiate the cache instance
 * val cache = cache<Int, Int> { it * 2 }
 *     .cachedInMemory(cachingJob)
 *     .expireAfter(2.minutes, cachingScope)
 *     .blocking()
 *
 * // Access the cache
 * println(cache[1]) // 2
 *
 * // Force the cache to accept another value
 * cache[1] = 3
 * println(cache[1]) // 3
 *
 * // Force the cache to forget the value, meaning a new request will be started on next access
 * cache.expire(1)
 * println(cache[1]) // 2
 *
 * // Don't forget to stop the cache workers when you're done using the cache
 * cachingJob.cancel()
 * ```
 */
fun <I, F, T> Cache<I, F, T>.blocking() =
	BlockingCache(this)
