package opensavvy.cache

import arrow.core.continuations.EffectScope
import arrow.core.continuations.either
import kotlinx.coroutines.flow.Flow
import opensavvy.state.Failure
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.captureProgress

/**
 * Cache implementation aimed to be the first link in a cache chain.
 *
 * This is not a valid implementation of a cache (it doesn't do any caching), and only serves as a link between caches
 * and the underlying network APIs.
 */
class CacheAdapter<I, T>(
	val query: suspend (I) -> Outcome<T>,
) : Cache<I, T> {

	override fun get(id: I): Flow<ProgressiveOutcome<T>> = captureProgress { query(id) }

	override suspend fun update(values: Collection<Pair<I, T>>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expire(ids: Collection<I>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expireAll() {
		// This cache layer has no state, nothing to do
	}

	companion object {
		fun <I, T> cache(transform: suspend EffectScope<Failure>.(I) -> T) =
			CacheAdapter<I, T> { either { transform(it) } }
	}
}
