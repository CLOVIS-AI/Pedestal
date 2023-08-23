package opensavvy.cache.contextual

import opensavvy.cache.Cache
import opensavvy.state.coroutines.ProgressiveFlow

/**
 * Implementation of [Cache] for a [ContextualCache].
 */
internal class ContextualWrapper<I, C, F, V>(
	private val upstream: ContextualCache<I, C, F, V>,
) : Cache<Pair<I, C>, F, V> {

	override fun get(id: Pair<I, C>): ProgressiveFlow<F, V> {
		val (ref, context) = id
		return upstream[ref, context]
	}

	override suspend fun update(values: Collection<Pair<Pair<I, C>, V>>) {
		upstream.update(values.map {
			val (identifier, value) = it
			val (id, context) = identifier
			Triple(id, context, value)
		})
	}

	override suspend fun expire(ids: Collection<Pair<I, C>>) {
		upstream.expireContextual(ids)
	}

	override suspend fun expireAll() {
		upstream.expireAll()
	}
}
