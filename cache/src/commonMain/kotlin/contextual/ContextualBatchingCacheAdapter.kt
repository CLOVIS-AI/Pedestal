package opensavvy.cache.contextual

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import opensavvy.cache.BatchingCacheAdapter
import opensavvy.cache.Cache
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.ProgressiveOutcome

internal class ContextualBatchingCacheAdapter<I, C, F, T>(
	scope: CoroutineScope,
	workers: Int,
	queryBatch: (Set<Pair<I, C>>) -> Flow<Triple<I, C, ProgressiveOutcome<F, T>>>,
) : ContextualCache<I, C, F, T> {

	private val batching = BatchingCacheAdapter(
		scope,
		workers,
	) {
		queryBatch(it)
			.map { (id, context, value) -> id to context to value }
	}

	override fun get(id: I, context: C): ProgressiveFlow<F, T> =
		batching[id to context]

	override suspend fun update(values: Collection<Triple<I, C, T>>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expire(ids: Collection<I>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expireContextual(ids: Collection<Pair<I, C>>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expireAll() {
		// This cache layer has no state, nothing to do
	}

}

/**
 * Cache implementation which collects cache requests into batches which are queried at the same time.
 *
 * This adapter is meant to be used as the first layer in a layer chain. By itself, it does no caching (all calls to [get][ContextualCache.get] call [transform]).
 * To learn more about layer chaining, see [Cache].
 * To learn more about the type parameters, see [ContextualCache].
 *
 * @see opensavvy.cache.batchingCache Non-contextual equivalent
 */
fun <I, C, F, T> batchingCache(
	scope: CoroutineScope,
	workers: Int = 1,
	transform: suspend FlowCollector<Triple<I, C, ProgressiveOutcome<F, T>>>.(Set<Pair<I, C>>) -> Unit,
): ContextualCache<I, C, F, T> = ContextualBatchingCacheAdapter(
	scope,
	workers,
) {
	flow {
		transform(it)
	}
}
