package opensavvy.backbone.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import opensavvy.backbone.Cache
import opensavvy.backbone.Data
import opensavvy.backbone.Ref
import opensavvy.backbone.cache.ExpirationCache.Companion.expireAfter
import opensavvy.logger.Logger.Companion.debug
import opensavvy.logger.loggerFor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
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
class ExpirationCache<O>(
	/**
	 * The previous cache layer, from which values will be expired.
	 */
	upstream: Cache<O>,
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
	context: CoroutineContext = EmptyCoroutineContext,
) : Cache.CacheLayer<O>(upstream) {
	private val lastUpdate = HashMap<Ref<O>, Instant>()
	private val lock = Semaphore(1)

	private val job = Job(context[Job])
	private val scope = CoroutineScope(job + CoroutineName("ExpirationCache task"))

	init {
		scope.launch {
			while (isActive) {
				delay(expireAfter)
				log.debug { "Starting a cleanup job for expired cache values" }

				lock.withPermit {
					val now = Clock.System.now()
					val iterator= lastUpdate.iterator()
					while (iterator.hasNext()) {
						val (_, instant) = iterator.next()

						if (instant < now - expireAfter)
							iterator.remove()
					}
				}
			}
		}
	}

	private suspend fun markAsUpdatedNow(ref: Ref<O>) {
		lock.withPermit {
			lastUpdate[ref] = Clock.System.now()
		}
	}

	override fun get(ref: Ref<O>): Flow<Data<O>> = upstream[ref]
		.onEach { markAsUpdatedNow(ref) }

	override suspend fun updateAll(values: Iterable<Data<O>>) {
		for (value in values)
			markAsUpdatedNow(value.ref)

		// Ensure the previous layers are updated as well
		super.updateAll(values)
	}

	override suspend fun expireAll(refs: Iterable<Ref<O>>) {
		for (ref in refs)
			lock.withPermit {
				lastUpdate.remove(ref)
			}
	}

	override suspend fun expireAll() {
		lock.withPermit {
			lastUpdate.clear()
		}
	}

	companion object {
		private val log = loggerFor(this)

		/**
		 * Factory function to easily add a [ExpirationCache] layer to an existing cache chain.
		 *
		 * @see ExpirationCache
		 */
		fun <O> Cache<O>.expireAfter(duration: Duration, context: CoroutineContext = EmptyCoroutineContext) = ExpirationCache(this, duration, context)
	}
}
