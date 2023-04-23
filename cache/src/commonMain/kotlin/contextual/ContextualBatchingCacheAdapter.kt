package opensavvy.cache.contextual

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import opensavvy.cache.BatchingCacheAdapter
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.failure.Failure
import opensavvy.state.progressive.ProgressiveOutcome

class ContextualBatchingCacheAdapter<I, C, F : Failure, T>(
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

fun <I, C, F : Failure, T> batchingCache(
	scope: CoroutineScope,
	workers: Int = 1,
	transform: suspend FlowCollector<Triple<I, C, ProgressiveOutcome<F, T>>>.(Set<Pair<I, C>>) -> Unit,
) = ContextualBatchingCacheAdapter<I, C, F, T>(
	scope,
	workers,
) {
	flow {
		transform(it)
	}
}
