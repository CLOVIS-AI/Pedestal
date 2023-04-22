package opensavvy.cache.contextual

import kotlinx.coroutines.CoroutineScope
import opensavvy.cache.expireAfter
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.failure.Failure
import kotlin.time.Duration

class ContextualExpirationCache<I, C, F : Failure, T>(
	private val upstream: ContextualCache<I, C, F, T>,
	duration: Duration,
	scope: CoroutineScope,
) : ContextualCache<I, C, F, T> {

	private val cache = ContextualWrapper(upstream)
		.expireAfter(duration, scope)

	override fun get(id: I, context: C): ProgressiveFlow<F, T> =
		cache[id to context]

	override suspend fun update(values: Collection<Triple<I, C, T>>) =
		cache.update(values.map { (id, context, value) -> id to context to value })

	override suspend fun expire(ids: Collection<I>) {
		// ExpirationCache doesn't store data, so we can directly expire the upstream
		upstream.expire(ids)
	}

	override suspend fun expireContextual(ids: Collection<Pair<I, C>>) {
		// ExpirationCache doesn't store data, so we can directly expire the upstream
		upstream.expireContextual(ids)
	}

	override suspend fun expireAll() {
		// ExpirationCache doesn't store data, so we can directly expire the upstream
		upstream.expireAll()
	}

}

fun <I, C, F : Failure, T> ContextualCache<I, C, F, T>.expireAfter(duration: Duration, scope: CoroutineScope) =
	ContextualExpirationCache(this, duration, scope)
