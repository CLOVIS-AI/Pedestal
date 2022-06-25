package opensavvy.backbone

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import opensavvy.backbone.Cache.Default
import opensavvy.backbone.Ref.Companion.directRequest

/**
 * Stores information temporarily to avoid unneeded network requests.
 *
 * The value stored behind a reference can be accessed through the [get] operator.
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
 * - Query the data for real afterwards.
 *
 * Cache chaining is instantiated in the opposite order, like iterators (the last in the chain is the first checked,
 * and delegates to the previous one if they do not have the value).
 * The first element of the chain, and therefore the one responsible for actually starting the request, is [Default].
 * Note that [Default] has a few implementation differences, it is not recommended to use it directly without chaining it under another implementation.
 */
interface Cache<O> {

	/**
	 * Gets the value associated with a [ref] in this cache.
	 *
	 * This function returns a [Flow] synchronously: it is safe to call in synchronous-only areas of the program, such
	 * as inside the body of a UI component.
	 * You can then subscribe to the [Flow] to access the actual values.
	 *
	 * For example, here is it how using a value in a React application could look like:
	 * ```kotlin
	 * fun Component(props) {
	 *     // Query the cache
	 *     val flow = props.cache[props.ref]
	 *     // Create a React-styled state so React re-renders
	 *     // the component when the cache returns a new value
	 *     var data by useState(props.ref.initialData)
	 *
	 *     // Asynchronously wait for new values in the cache
	 *     useEffect {
	 *         val job = Job()
	 *
	 *         // Subscribe to new events from the cache, and assign them to
	 *         // the React state to tell React when a re-render is necessary
	 *         CoroutineScope(job).launch {
	 *             flow.collect { data = it }
	 *         }
	 *
	 *         // When the component is unmounted, cancel the asynchronous
	 *         // job (e.g. unsubscribe from the flow)
	 *         cleanup {
	 *             job.cancel()
	 *         }
	 *     }
	 * }
	 * ```
	 * Of course, in the real world, this machinery could be hidden behind a custom hook
	 * (e.g. in [Compose](https://developer.android.com/reference/kotlin/androidx/compose/runtime/package-summary#(kotlinx.coroutines.flow.Flow).collectAsState(kotlin.Any,kotlin.coroutines.CoroutineContext))))).
	 * However, this is a good demonstration of how to adapt flows to any reactive framework.
	 *
	 */
	operator fun get(ref: Ref<O>): Flow<Data<O>>

	/**
	 * Force the cache to accept that [value] is a more recent value for the given [ref] than whatever it was previously
	 * storing.
	 *
	 * This is a convenience method around [updateAll].
	 */
	suspend fun update(ref: Ref<O>, value: O) {
		update(Data(Result.Success(value), Data.Status.Completed, ref))
	}

	/**
	 * Force the cache to accept that [value] is a more recent value for the given [Data.ref] than whatever it was
	 * previously storing.
	 *
	 * This is a convenience method around [updateAll].
	 */
	suspend fun update(value: Data<O>) {
		updateAll(listOf(value))
	}

	/**
	 * Force the cache to accept that the given [values] are more recent values for the given [Data.ref] than
	 * whatever it was previously storing.
	 *
	 * Values are updated in all cache layers.
	 *
	 * If multiple values are provided for the same reference, a cache implementation may either:
	 * - take only the first occurrence into account,
	 * - take only the last occurrence into account,
	 * - take all occurrences into account, as if `updateAll` was called once consecutively with each occurrence, in the same order as their appearance in the iterable.
	 *
	 * If [values] is empty, this function does nothing.
	 *
	 * For convenience, you may use [update] instead.
	 */
	suspend fun updateAll(values: Iterable<Data<O>>)

	/**
	 * Force the cache to accept that whatever value is currently associated with the given [ref] is invalid and
	 * should be forgotten.
	 *
	 * The next call of [get] for the given reference will query the value from scratch instead of using the cache.
	 *
	 * To expire a value and immediately start a query, see [refresh].
	 *
	 * This is a convenience method around [expireAll].
	 */
	suspend fun expire(ref: Ref<O>) {
		expireAll(listOf(ref))
	}

	/**
	 * Force the cache to accept that whatever values are currently associated with the given [refs] are invalid and
	 * should be forgotten.
	 *
	 * The next call of [get] for any of the given references will query the value from scratch instead of using the cache.
	 * Therefore, calling this function with an empty iterable does nothing.
	 *
	 * To expire a single reference, you may use [expire] instead.
	 * To expire all values, use [expireAll] instead.
	 */
	suspend fun expireAll(refs: Iterable<Ref<O>>)

	/**
	 * Expires all the given values for this layer and the previous ones.
	 *
	 * @see expireAll
	 */
	suspend fun expireAllRecursively(refs: Iterable<Ref<O>>)

	/**
	 * Force the cache to forget its entire internal state.
	 *
	 * The next call of [get] on any reference will query the value from scratch instead of using the cache.
	 *
	 * To expire selected references, use [expire] and [expireAll] instead.
	 */
	suspend fun expireAll()

	/**
	 * Expires all values for this layer and the previous ones.
	 *
	 * @see expireAll
	 */
	suspend fun expireAllRecursively()

	/**
	 * Default cache implementation aimed to be used as the first link in a cache chain.
	 *
	 * This is not actually a valid implementation of a cache (it doesn't do any caching) and only serves as a link
	 * between caches and the underlying network APIs (via [Backbone]), it takes a few liberties with the API:
	 * - [get] doesn't actually return a long-lived flow,
	 * - [updateAll] and [expireAll] don't actually do anything.
	 */
	class Default<O> : Cache<O> {
		override fun get(ref: Ref<O>): Flow<Data<O>> = flow {
			emit(Data(Result.NoData, Data.Status.Loading.Basic(), ref))

			// Request the backbone and emit all the results
			val result = ref.directRequest()
			emitAll(result)
		}

		override suspend fun updateAll(values: Iterable<Data<O>>) {
			// This has no state, there is nothing to update
		}

		override suspend fun expireAll(refs: Iterable<Ref<O>>) {
			// This has no state, there is nothing to expire
		}

		override suspend fun expireAll() {
			// This has no state, there is nothing to expire
		}

		override suspend fun expireAllRecursively() {
			// This has no state, there is nothing to expire
		}

		override suspend fun expireAllRecursively(refs: Iterable<Ref<O>>) {
			// This has no state, there is nothing to expire
		}
	}

	/**
	 * Simple implementation that handles delegating to an [upstream] cache layer the functions that should be delegated.
	 */
	abstract class CacheLayer<O>(protected val upstream: Cache<O>) : Cache<O> {
		override suspend fun updateAll(values: Iterable<Data<O>>) {
			upstream.updateAll(values)
		}

		override suspend fun expireAllRecursively() {
			expireAll()
			upstream.expireAllRecursively()
		}

		override suspend fun expireAllRecursively(refs: Iterable<Ref<O>>) {
			expireAll(refs)
			upstream.expireAllRecursively(refs)
		}
	}

	companion object {
		/**
		 * Force the cache to request the value again, and start a new query.
		 *
		 * This is the same as calling [expire] followed by [get].
		 */
		suspend fun <O> Cache<O>.refresh(ref: Ref<O>): Flow<Data<O>> {
			expire(ref)
			return get(ref)
		}
	}
}
