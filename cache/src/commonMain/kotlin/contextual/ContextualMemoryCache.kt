package opensavvy.cache.contextual

import kotlinx.coroutines.Job
import opensavvy.cache.cachedInMemory
import opensavvy.state.coroutines.ProgressiveFlow

class ContextualMemoryCache<I, C, F, T>(
	upstream: ContextualCache<I, C, F, T>,
	job: Job,
) : ContextualCache<I, C, F, T> {

	private val cache = ContextualWrapper(upstream)
		.cachedInMemory(job)

	override fun get(id: I, context: C): ProgressiveFlow<F, T> =
		cache[id to context]

	override suspend fun update(values: Collection<Triple<I, C, T>>) =
		cache.update(values.map { (id, context, value) -> id to context to value })

	override suspend fun expire(ids: Collection<I>) {
		val filterIds = ids.toSet()

		cache.expireIf { (id, _) -> id in filterIds }
	}

	override suspend fun expireContextual(ids: Collection<Pair<I, C>>) =
		cache.expire(ids)

	override suspend fun expireAll() =
		cache.expireAll()

}

fun <I, C, F, T> ContextualCache<I, C, F, T>.cachedInMemory(job: Job) =
	ContextualMemoryCache(this, job)
