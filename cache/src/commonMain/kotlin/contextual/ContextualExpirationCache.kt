package opensavvy.cache.contextual

import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import opensavvy.cache.expireAfter
import opensavvy.state.coroutines.ProgressiveFlow
import kotlin.time.Duration

internal class ContextualExpirationCache<I, C, F, V>(
	private val upstream: ContextualCache<I, C, F, V>,
	duration: Duration,
	clock: Clock,
	scope: CoroutineScope,
) : ContextualCache<I, C, F, V> {

	private val cache = ContextualWrapper(upstream)
		.expireAfter(duration, scope, clock)

	override fun get(id: I, context: C): ProgressiveFlow<F, V> =
		cache[id to context]

	override suspend fun update(values: Collection<Triple<I, C, V>>) =
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

/**
 * Age-based [ContextualCache] expiration strategy.
 *
 * @see opensavvy.cache.expireAfter Non-contextual equivalent
 */
fun <Identifier, Context, Failure, Value> ContextualCache<Identifier, Context, Failure, Value>.expireAfter(duration: Duration, scope: CoroutineScope, clock: Clock): ContextualCache<Identifier, Context, Failure, Value> =
	ContextualExpirationCache(this, duration, clock, scope)

@Suppress("DeprecatedCallableAddReplaceWith")
@Deprecated(message = "Specifying the clock explicitly will become mandatory in 2.0.")
fun <Identifier, Context, Failure, Value> ContextualCache<Identifier, Context, Failure, Value>.expireAfter(duration: Duration, scope: CoroutineScope): ContextualCache<Identifier, Context, Failure, Value> =
	ContextualExpirationCache(this, duration, Clock.System, scope)
