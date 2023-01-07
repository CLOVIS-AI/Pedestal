package opensavvy.cache

import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Marker interface for coroutine context elements that should pass through the cache.
 *
 * On a cache miss, a cache may start a new request.
 * In that case, all coroutine context elements implementing this interface will be retained for the real request.
 */
interface PassThroughContext : CoroutineContext {

	companion object {

		internal fun CoroutineContext.onlyPassThrough() = fold(EmptyCoroutineContext as CoroutineContext) { acc, it ->
			if (it is PassThroughContext) acc + it
			else acc
		}

	}
}
