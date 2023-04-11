package opensavvy.backbone

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import opensavvy.backbone.Ref.Companion.directRequest
import opensavvy.cache.BatchingCacheAdapter
import opensavvy.cache.Cache
import opensavvy.cache.CacheAdapter
import opensavvy.state.coroutines.captureProgress
import opensavvy.state.failure.Failure

typealias RefCache<F, O> = Cache<Ref<F, O>, F, O>

fun <F : Failure, O> defaultRefCache() = CacheAdapter<Ref<F, O>, F, O> { it.directRequest() }

fun <F : Failure, O> batchingRefCache(scope: CoroutineScope, workers: Int = 1) =
	BatchingCacheAdapter<Ref<F, O>, F, O>(scope, workers) { requests ->
		val backbones = requests
			.groupBy { it.backbone }

		flow {
			// This implementation is sequential
			// Because the actual network request is batched, we assume that the sequential reading of the results is not
			// an issue.
			// If it ever becomes one, this function should be rewritten to wait for the results in parallel.

			for ((backbone, refs) in backbones) {
				val results = backbone.batchRequests(refs.toHashSet())
				for ((ref, result) in results) {
					emitAll(
						captureProgress { result() }
							.map { ref to it }
					)
				}
			}
		}
	}
