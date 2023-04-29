package opensavvy.cache.contextual

import kotlinx.coroutines.flow.Flow
import opensavvy.cache.Cache
import opensavvy.state.coroutines.ProgressiveFlow

/**
 * Stores information temporarily to avoid unneeded network requests.
 *
 * Unlike [Cache], a contextual cache stores multiple values per identifier.
 * The context decides which value is visible.
 * Here are a few examples of context usage:
 * - authentication information (different users see different values)
 * - paging information (different values are returned depending on the requested page)
 *
 * The main advantage of using this interface rather than using a compound key in [Cache] is that it is possible to
 * expire a value for all contexts.
 *
 * In all other regards (cache states, cache chaining…), this interface is identical to [Cache].
 * [Cache] can be seen as a specialization of this interface for the case where the context is [Unit].
 *
 * @param I The identifier used to request from the cache.
 * @param C The context which differentiates between cache results.
 * @param F The possible failures when requesting the cache.
 * @param T The possible successful value when requesting the cache.
 */
interface ContextualCache<I, C, F, T> {

	/**
	 * Gets the value associated with an [id] and a [context] in this cache.
	 *
	 * This function returns a [Flow] instance synchronously: it is safe to call in synchronous-only areas of the
	 * program, such as inside the body of a UI component. You can then subscribe to the [Flow] to access the actual
	 * values.
	 */
	operator fun get(id: I, context: C): ProgressiveFlow<F, T>

	/**
	 * Forces the cache to accept [value] as a more recent value for the given [id] and [context] than whatever it
	 * was previously storing.
	 *
	 * All layers are updated.
	 */
	suspend fun update(id: I, context: C, value: T) {
		update(listOf(Triple(id, context, value)))
	}

	/**
	 * Forces the cache to accept the given [values] as more recent for their associated identifier than whatever
	 * was previously stored.
	 *
	 * All layers are updated.
	 */
	suspend fun update(values: Collection<Triple<I, C, T>>)

	/**
	 * Tells the cache that the values it stores for the given [id] are out of date, no matter the context,
	 * and should be queried again the next time they are requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expire(id: I) {
		expire(listOf(id))
	}

	/**
	 * Tells the cache that the value it stores for the given [id] and [context] is out of date,
	 * and should be queried again the next time they are requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expire(id: I, context: C) {
		expireContextual(listOf(id to context))
	}

	/**
	 * Tells the cache that the values it stores for the given [ids] are out of date, no matter the context,
	 * and should be queried again the next time they are requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expire(ids: Collection<I>)

	/**
	 * Tells the cache that the values it stores for the given [ids] are out of date,
	 * and should be queried again the next time they are requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expireContextual(ids: Collection<Pair<I, C>>)

	/**
	 * Tells the cache that all values are out of date, and should be queried again the next time they are requested.
	 *
	 * All layers are updated.
	 */
	suspend fun expireAll()

}
