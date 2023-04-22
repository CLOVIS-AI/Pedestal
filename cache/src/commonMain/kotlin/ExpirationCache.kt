package opensavvy.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import opensavvy.cache.ExpirationCache.Companion.expireAfter
import opensavvy.logger.Logger.Companion.trace
import opensavvy.logger.loggerFor
import opensavvy.progress.done
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.failure.Failure
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Cache layer that expires values from the previous layer after a specified [duration][expireAfter].
 *
 * To add an [ExpirationCache] to a previous layer, use [Cache.expireAfter][ExpirationCache.Companion.expireAfter]:
 * ```kotlin
 * val cache = Cache.Default
 *      .expireAfter(5.minutes, Job())
 * ```
 */
class ExpirationCache<I, F : Failure, T>(
	/**
	 * The previous cache layer, from which values will be expired.
	 */
	private val upstream: Cache<I, F, T>,
	/**
	 * After how much time should the values from the previous cache layer be expired.
	 *
	 * The values will not be expired exactly this time after the last update.
	 * Instead, the implementation guarantees that the data will be expired between [expireAfter] and [expireAfter] * 2.
	 */
	private val expireAfter: Duration = 5.minutes,
	/**
	 * The asynchronous context in which the cleaner runs.
	 *
	 * Cancelling this job will cancel the expiration job, after which this cache will stop expiring data.
	 */
	expirationScope: CoroutineScope,
) : Cache<I, F, T> {
	private val log = loggerFor(this)

	private val lastUpdate = HashMap<I, Instant>()
	private val lock = Mutex()

	init {
		expirationScope.launch(CoroutineName("$this")) {
			while (isActive) {
				delay(expireAfter)

				lock.withLock("checkExpiredValues()") {
					val now = Clock.System.now()
					val iterator = lastUpdate.iterator()
					while (iterator.hasNext()) {
						val (id, instant) = iterator.next()

						if (instant < now - expireAfter) {
							log.trace(id) { "Expired value:" }
							iterator.remove()
							upstream.expire(id)
						}
					}
				}
			}
		}
	}

	private suspend fun markAsUpdatedNow(id: I) {
		lock.withLock("markAsUpdatedNow($id)") {
			log.trace(id) { "Updated now:" }
			lastUpdate[id] = Clock.System.now()
		}
	}

	override fun get(id: I): ProgressiveFlow<F, T> = upstream[id]
		.onEach {
			if (it.progress == done())
				markAsUpdatedNow(id)
			// else: it's still loading, no need to count it as done
		}

	override suspend fun update(values: Collection<Pair<I, T>>) {
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

	companion object {
		/**
		 * Factory function to easily add a [ExpirationCache] layer to an existing cache chain.
		 *
		 * @see ExpirationCache
		 */
		fun <I, F : Failure, T> Cache<I, F, T>.expireAfter(duration: Duration, scope: CoroutineScope) =
			ExpirationCache(this, duration, scope)
	}
}
