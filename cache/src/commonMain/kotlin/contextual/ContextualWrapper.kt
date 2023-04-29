package opensavvy.cache.contextual

import opensavvy.cache.Cache
import opensavvy.state.coroutines.ProgressiveFlow

/**
 * Implementation of [Cache] for a [ContextualCache].
 */
internal class ContextualWrapper<I, C, F, T>(
	private val upstream: ContextualCache<I, C, F, T>,
) : Cache<Pair<I, C>, F, T> {

	override fun get(id: Pair<I, C>): ProgressiveFlow<F, T> {
		val (ref, context) = id
		return upstream[ref, context]
	}

	override suspend fun update(values: Collection<Pair<Pair<I, C>, T>>) {
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
