package opensavvy.cache.blocking

import kotlinx.coroutines.runBlocking
import opensavvy.cache.Cache
import opensavvy.cache.InfallibleCache
import opensavvy.cache.contextual.ContextualCache
import opensavvy.state.coroutines.now
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.value

/**
 * A blocking cache wrapper, which offers different results depending on the context.
 *
 * Unlike [asynchronous caches][ContextualCache], this implementation doesn't allow subscribing to a value to see it change over time.
 *
 * To instantiate this class, see the [blocking] helper.
 *
 * @param I The identifier used to request from the cache
 * @param C The context which differentiates between cache results.
 * @param F The possible failures when requesting from the cache.
 * @param T The possible successful values when requesting from the cache.
 */
class BlockingContextualCache<I, C, F, T>(
	private val upstream: ContextualCache<I, C, F, T>,
) {

	/**
	 * Gets the value associated with [id] and [context] in the cache, at the current time.
	 *
	 * Unlike [ContextualCache.get], this function does not allow subscribing to the value to see its changes over time.
	 */
	operator fun get(id: I, context: C): Outcome<F, T> = runBlocking {
		upstream[id, context].now()
	}

	/**
	 * Forces the cache to accept [value] as a more recent value for the given [id] and [context] than whatever it was previously storing.
	 *
	 * For more information, see [ContextualCache.update].
	 */
	operator fun set(id: I, context: C, value: T) = runBlocking {
		upstream.update(id, context, value)
	}

	/**
	 * Forces the cache to accept the given [values] as more recent than their associated identifier than whatever was
	 * previously stored.
	 *
	 * For more information, see [ContextualCache.update].
	 */
	fun update(values: Collection<Triple<I, C, T>>) = runBlocking {
		upstream.update(values)
	}

	/**
	 * Forces the cache to accept the given [values] as more recent than their associated identifier than whatever was
	 * previously stored.
	 *
	 * For more information, see [ContextualCache.update].
	 */
	fun update(vararg values: Triple<I, C, T>) = update(values.asList())

	/**
	 * Tells the cache that the value it stores for [id] is out-of-date for all contexts, and should be queried again the next time it is requested.
	 *
	 * For more information, see [ContextualCache.expire].
	 */
	fun expire(id: I) = runBlocking {
		upstream.expire(id)
	}

	/**
	 * Tells the cache that the value it stores for [id] and [context] is out-of-date for all contexts, and should be queried again the next time it is requested.
	 *
	 * For more information, see [ContextualCache.expire].
	 */
	fun expire(id: I, context: C) = runBlocking {
		upstream.expire(id, context)
	}

	/**
	 * Tells the cache that the value it stores for the given [ids] are out-of-date for all contexts, and should be queried again next time they are requested.
	 *
	 * For more information, see [ContextualCache.expire].
	 */
	fun expire(ids: Collection<I>) = runBlocking {
		upstream.expire(ids)
	}

	/**
	 * Tells the cache that the value it stores for the given [ids] and contexts are out-of-date, and should be queried again next time they are requested.
	 *
	 * For more information, see [ContextualCache.expire].
	 */
	fun expireContextual(ids: Collection<Pair<I, C>>) = runBlocking {
		upstream.expireContextual(ids)
	}

	/**
	 * Tells the cache that all values are out-of-date, and should be queried again the next time they are requested.
	 *
	 * For more information, see [ContextualCache.expire].
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
fun <I, C, T> BlockingContextualCache<I, C, Nothing, T>.getValue(id: I, context: C): T =
	get(id, context).value

/**
 * Converts an [asynchronous contextual cache][ContextualCache] into a [blocking cache][BlockingContextualCache].
 *
 * ### Example
 *
 * ```kotlin
 * // Create the coroutine context
 * val cachingJob = SupervisorJob()
 * val cachingScope = CoroutineScope(cachingJob)
 *
 * class User(val isAdmin: Boolean)
 * val admin = User(true)
 * val user = User(false)
 *
 * // Instantiate the cache instance
 * val cache = cache<Int, User, Nothing, Int?> { it, user ->
 *     if (user.isAdmin)
 *         (it * 2).success()
 *     else
 *         null.success()
 * }
 *     .cachedInMemory(cachingJob)
 *     .expireAfter(2.minutes, cachingScope)
 *     .blocking()
 *
 * // Access the cache as an admin
 * println(cache[1, admin]) // 2
 *
 * // Access the cache as a regular user
 * println(cache[1, user])  // null
 *
 * // Force the cache to accept another value
 * cache[1, user] = 3
 * println(cache[1, user])  // 3
 *
 * // Force the cache to forget the value for a specific user, meaning a new request will be started on next access
 * cache.expire(1, user)
 * println(cache[1, user])  // null
 *
 * // Force the cache to forget the value for all users
 * cache.expire(1)
 * println(cache[1, user]) // null
 *
 * // Don't forget to stop the cache workers when you're done using the cache
 * cachingJob.cancel()
 * ```
 */
fun <I, C, F, T> ContextualCache<I, C, F, T>.blocking() =
	BlockingContextualCache(this)
