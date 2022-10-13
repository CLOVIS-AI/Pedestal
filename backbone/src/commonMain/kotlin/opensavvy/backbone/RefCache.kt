package opensavvy.backbone

import kotlinx.coroutines.flow.emitAll
import opensavvy.backbone.Ref.Companion.directRequest
import opensavvy.cache.BatchingCacheAdapter
import opensavvy.cache.Cache
import opensavvy.cache.CacheAdapter
import opensavvy.state.state
import kotlin.coroutines.CoroutineContext

typealias RefCache<O> = Cache<Ref<O>, O>

fun <O> defaultRefCache() = CacheAdapter<Ref<O>, O> { it.directRequest() }

fun <O> batchingRefCache(context: CoroutineContext, workers: Int = 1) =
	BatchingCacheAdapter<Ref<O>, O>(context, workers) { requests ->
		val results = requests.groupBy { it.backbone }
			.map { (backbone, refs) -> backbone.batchRequests(refs.toHashSet()) }

		state {
			for (result in results)
				emitAll(result)
		}
	}
