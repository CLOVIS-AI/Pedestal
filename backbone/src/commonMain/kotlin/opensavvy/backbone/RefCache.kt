package opensavvy.backbone

import kotlinx.coroutines.flow.flow
import opensavvy.backbone.Ref.Companion.directRequest
import opensavvy.cache.BatchingCacheAdapter
import opensavvy.cache.Cache
import opensavvy.cache.CacheAdapter
import kotlin.coroutines.CoroutineContext

typealias RefCache<O> = Cache<Ref<O>, O>

fun <O> defaultRefCache() = CacheAdapter<Ref<O>, O> { it.directRequest() }

fun <O> batchingRefCache(context: CoroutineContext, workers: Int = 1) =
	BatchingCacheAdapter<Ref<O>, O>(context, workers) { requests ->
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
					emit(ref to result())
				}
			}
		}
	}
