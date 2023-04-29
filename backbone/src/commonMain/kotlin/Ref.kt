package opensavvy.backbone

import opensavvy.cache.Cache
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.coroutines.now

/**
 * A reference to a specific [object][O].
 *
 * A reference is a small object that allows to pass around an object from an API without querying it.
 * A reference should always be immutable.
 * Each reference has a matching [Backbone] object responsible for managing it.
 *
 * [Ref] implementation should ensure that their [equals] and [hashCode] functions are correct.
 *
 * To access the value behind a reference, use [request].
 *
 * ### Note for implementors
 *
 * When implementing this interface, it is common to provide functions to all mutating methods from the matching
 * [Backbone] as wrappers to it. This makes using the reference easier.
 *
 * @param O The object this reference refers to.
 * @param F Failures that may be returned when calling [request].
 */
interface Ref<F, O> {

	/**
	 * Requests the referenced data.
	 *
	 * It is encouraged, but not mandatory, to implement this method using [Cache].
	 */
	fun request(): ProgressiveFlow<F, O>

	companion object
}

/**
 * Requests the referenced data, returning the first value returned by the cache.
 *
 * This function returns a single value and not a subscription, it is not recommended to use it when being notified of
 * new values is important (e.g. in a UI), in which case you should use [Ref.request].
 * This function is intended for non-reactive environments (e.g. server requests, tests…).
 */
suspend fun <F, O> Ref<F, O>.now() = request().now()
