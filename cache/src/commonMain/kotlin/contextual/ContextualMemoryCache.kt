package opensavvy.cache.contextual

import kotlinx.coroutines.Job
import opensavvy.cache.MemoryCache
import opensavvy.cache.cachedInMemory
import opensavvy.state.coroutines.ProgressiveFlow

internal class ContextualMemoryCache<I, C, F, V>(
	upstream: ContextualCache<I, C, F, V>,
	job: Job,
) : ContextualCache<I, C, F, V> {

	private val cache = ContextualWrapper(upstream)
		.cachedInMemory(job) as MemoryCache<Pair<I, C>, F, V>

	override fun get(id: I, context: C): ProgressiveFlow<F, V> =
		cache[id to context]

	override suspend fun update(values: Collection<Triple<I, C, V>>) =
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

/**
 * In-memory [ContextualCache] layer.
 *
 * @see opensavvy.cache.cachedInMemory Non-contextual equivalent
 */
fun <Identifier, Context, Failure, Value> ContextualCache<Identifier, Context, Failure, Value>.cachedInMemory(job: Job): ContextualCache<Identifier, Context, Failure, Value> =
	ContextualMemoryCache(this, job)
