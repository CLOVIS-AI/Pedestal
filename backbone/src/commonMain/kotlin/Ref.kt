package opensavvy.backbone

import opensavvy.backbone.Backbone.Companion.request
import opensavvy.backbone.Ref.Companion.directRequest
import opensavvy.backbone.Ref.Companion.request
import opensavvy.state.progressive.firstValue

/**
 * A reference to a specific [object][O].
 *
 * A reference is a small object that allows to pass around an object from an API without querying it.
 *
 * [Ref] implementation should ensure that their [equals] and [hashCode] functions are correct.
 * The [backbone] field for a particular reference should always return the same [Backbone] object.
 *
 * To access the value behind a reference, use [directRequest] or [request].
 *
 * @param O The object this reference refers to.
 */
interface Ref<O> {

	/**
	 * The [Backbone] responsible for this reference.
	 *
	 * This property should always return the same [Backbone] instance for a given [Ref].
	 */
	val backbone: Backbone<O>

	/**
	 * Most simple implementation of [Ref], which identifies objects with an [id].
	 */
	data class Basic<O>(val id: String, override val backbone: Backbone<O>) : Ref<O>

	companion object {
		/**
		 * Requests the referenced data (without taking into account the cache).
		 *
		 * This is a convenience method around [Backbone.directRequest].
		 */
		suspend fun <O> Ref<O>.directRequest() = backbone.directRequest(this)

		/**
		 * Requests the referenced data, returning a value from the cache if one is stored.
		 *
		 * This is a convenience method around [Backbone.request].
		 */
		fun <O> Ref<O>.request() = backbone.request(this)

		/**
		 * Requests the referenced data, returning the first value returned by the cache.
		 *
		 * This is a shorthand to `ref.request().firstValue()`.
		 *
		 * This function returns a single value and not a subscription, it is not recommended to use it when being
		 * notified of new values is important (e.g. in a UI).
		 * This function is intended for non-reactive environments (e.g. server requests, tests…).
		 */
		suspend fun <O> Ref<O>.now() = request().firstValue()

		/**
		 * Forces the cache to forget anything it might remember about this reference.
		 *
		 * The next time [request] is called, a new request will be started.
		 */
		suspend fun <O> Ref<O>.expire() {
			backbone.cache.expire(this)
		}
	}
}
