package opensavvy.backbone

import kotlinx.coroutines.flow.Flow
import opensavvy.backbone.Backbone.Companion.request
import opensavvy.backbone.Data.Companion.firstSuccessOrThrow
import opensavvy.backbone.Ref.Companion.directRequest
import opensavvy.backbone.Ref.Companion.request

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
		fun <O> Ref<O>.directRequest() = backbone.directRequest(this)

		/**
		 * Requests the referenced data, returning a value from the cache if one is stored.
		 *
		 * This is a convenience method around [Backbone.request].
		 */
		fun <O> Ref<O>.request() = backbone.request(this)

		/**
		 * Forces the cache to forget anything it might remember about this reference.
		 *
		 * The next time [request] is called, a new request will be started.
		 */
		suspend fun <O> Ref<O>.expire() {
			backbone.cache.expireAllRecursively(listOf(this))
		}

		/**
		 * Queries the value behind the reference, throws an exception if anything goes wrong.
		 *
		 * This method can be used to easily access the value behind a reference.
		 * However, because it returns a single value and not a [Flow], it is not possible to be notified when the value
		 * is modified.
		 * This method should therefore only be used in non-reactive contexts (tests, to write the value to the terminal, etc).
		 */
		suspend fun <O> Ref<O>.requestValue() = request().firstSuccessOrThrow()
	}
}
