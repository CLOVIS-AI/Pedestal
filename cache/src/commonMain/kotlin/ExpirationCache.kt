package opensavvy.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import opensavvy.logger.Logger.Companion.trace
import opensavvy.logger.loggerFor
import opensavvy.progress.done
import opensavvy.state.coroutines.ProgressiveFlow
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Cache layer that expires values from the previous layer after a specified [duration][expireAfter].
 *
 * To add an [ExpirationCache] to a previous layer, use [Cache.expireAfter][Cache.expireAfter]:
 * ```kotlin
 * val cache = Cache.Default
 *      .expireAfter(5.minutes, Job())
 * ```
 */
internal class ExpirationCache<I, F, V>(
	/**
	 * The previous cache layer, from which values will be expired.
	 */
	private val upstream: Cache<I, F, V>,
	/**
	 * After how much time should the values from the previous cache layer be expired.
	 *
	 * The values will not be expired exactly this time after the last update.
	 * Instead, the implementation guarantees that the data will be expired between [expireAfter] and [expireAfter] * 2.
	 */
	private val expireAfter: Duration = 5.minutes,
	private val clock: Clock,
	/**
	 * The asynchronous context in which the cleaner runs.
	 *
	 * Cancelling this job will cancel the expiration job, after which this cache will stop expiring data.
	 */
	expirationScope: CoroutineScope,
) : Cache<I, F, V> {
	private val log = loggerFor(this)

	private val lastUpdate = HashMap<I, Instant>()
	private val lock = Mutex()

	init {
		expirationScope.launch(CoroutineName("ExpirationCacheJob@${hashCode()}")) {
			log.trace { "Will expire in $expireAfter" } // TODO remove

			while (isActive) {
				delay(expireAfter)
				log.trace { "Performing an expiration run" } // TODO remove

				lock.withLock("checkExpiredValues()") {
					val now = clock.now()
					val iterator = lastUpdate.iterator()
					while (iterator.hasNext()) {
						val (id, instant) = iterator.next()

						if (instant <= now - expireAfter) {
							log.trace(id) { "Expired value:" }
							iterator.remove()
							upstream.expire(id)
						} else {
							log.trace(id, instant, now - expireAfter, instant < now - expireAfter) { "Will not expire this element, because it is too recent" } // TODO remove
						}
					}
				}
			}
		}
	}

	private suspend fun markAsUpdatedNow(id: I) {
		lock.withLock("markAsUpdatedNow($id)") {
			log.trace(id) { "Updated now:" }
			lastUpdate[id] = clock.now()
		}
	}

	override fun get(id: I): ProgressiveFlow<F, V> = upstream[id]
		.onEach {
			if (it.progress == done())
				markAsUpdatedNow(id)
			// else: it's still loading, no need to count it as done
		}

	override suspend fun update(values: Collection<Pair<I, V>>) {
		// When the upstream is updated, it will signal the modification through the 'get' function,
		// which will catch. No need to update anything else here.
		upstream.update(values)
	}

	override suspend fun expire(ids: Collection<I>) {
		for (ref in ids)
			lock.withLock("expire($ids)") {
				lastUpdate.remove(ref)
			}
		upstream.expire(ids)
	}

	override suspend fun expireAll() {
		lock.withLock("expireAll()") {
			lastUpdate.clear()
		}
		upstream.expireAll()
	}

	companion object
}

/**
 * Age-based [Cache] expiration strategy.
 *
 * ### General behavior
 *
 * The cache starts a worker in [scope]. Every [duration], all values which have not been updated
 * for at least [duration] are expired in the previous layer.
 *
 * This layer considers any non-loading value returned by [get][Cache.get] to be new.
 *
 * If [scope] is cancelled, requests made to this cache continue to work as normal, but no values are ever expired anymore.
 *
 * ### Example
 *
 * ```kotlin
 * val scope: CoroutineScope = …
 *
 * val powersOfTwo = cache<Int, Int> { it * 2 }
 *     .cachedInMemory(scope.coroutineContext.job)
 *     .expireAfter(10.minutes, scope, clock)
 * ```
 */
fun <Identifier, Failure, Value> Cache<Identifier, Failure, Value>.expireAfter(duration: Duration, scope: CoroutineScope, clock: Clock): Cache<Identifier, Failure, Value> =
	ExpirationCache(this, duration, clock, scope)
