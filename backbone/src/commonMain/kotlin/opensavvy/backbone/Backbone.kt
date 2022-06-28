package opensavvy.backbone

import kotlinx.coroutines.flow.Flow

/**
 * A common interface for API endpoints.
 *
 * A [Backbone] represents all endpoints for a specific data type.
 * Each method represents a different endpoint to implement actions on the relevant objects (list all objects,
 * create a new one, etc).
 */
interface Backbone<O> {

	/**
	 * A cache used to store previous results of [directRequest].
	 *
	 * As a convenience, you can use [request] to launch a request through the cache.
	 */
	val cache: Cache<O>

	/**
	 * Fetches the value associated with a [ref] in an external media (e.g. a remote server, a database).
	 *
	 * This function completely bypasses the [cache]: a request will be sent everytime it is called.
	 * To avoid sending unnecessary requests, use [request] instead.
	 *
	 * The returned flow is **short-lived**: it is closed after the request finishes.
	 */
	fun directRequest(ref: Ref<O>): Flow<Data<O>>

	companion object {
		/**
		 * Fetches the value associated with a [ref] in an external media (e.g. a remote server, a database).
		 *
		 * This function takes the [cache] into account, it will return a previous value if there is one stored.
		 * To force sending a request, use [directRequest] instead.
		 */
		fun <O> Backbone<O>.request(ref: Ref<O>) = cache[ref]
	}
}
