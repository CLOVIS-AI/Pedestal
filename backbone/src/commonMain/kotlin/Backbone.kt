package opensavvy.backbone

import opensavvy.state.failure.Failure
import opensavvy.state.outcome.Outcome

/**
 * A common interface for API endpoints.
 *
 * A [Backbone] represents all endpoints for a specific data type.
 * Each method represents a different endpoint to implement actions on the relevant objects (list all objects,
 * create a new one, etc).
 */
interface Backbone<F : Failure, O> {

	/**
	 * A cache used to store previous results of [directRequest].
	 *
	 * As a convenience, you can use [request] to launch a request through the cache.
	 */
	val cache: RefCache<F, O>

	/**
	 * Fetches the value associated with a [ref] in an external media (e.g. a remote server, a database).
	 *
	 * This function completely bypasses the [cache]: a request will be sent everytime it is called.
	 * To avoid sending unnecessary requests, use [request] instead.
	 */
	suspend fun directRequest(ref: Ref<F, O>): Outcome<F, O>

	/**
	 * Fetches the value associated with all [refs] in an external media (e.g. a remote server, a database).
	 *
	 * This function completely bypasses the [cache]: a request will be sent everytime this is called.
	 * To avoid sending unnecessary requests, use [request] instead.
	 *
	 * The returned flow is **short-lived**: it is closed after the request finishes.
	 * The updates regarding the various references are not ordered between each other.
	 *
	 * To fetch a single value, see [directRequest].
	 *
	 * The default implementation simply calls [directRequest] sequentially.
	 */
	fun batchRequests(refs: Set<Ref<F, O>>): Map<Ref<F, O>, suspend () -> Outcome<F, O>> = refs
		.map { it to suspend { directRequest(it) } }
		.associate { it }

	companion object {
		/**
		 * Fetches the value associated with a [ref] in an external media (e.g. a remote server, a database).
		 *
		 * This function takes the [cache] into account, it will return a previous value if there is one stored.
		 * To force sending a request, use [directRequest] instead.
		 */
		fun <F : Failure, O> Backbone<F, O>.request(ref: Ref<F, O>) = cache[ref]
	}
}
