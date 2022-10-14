package opensavvy.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import opensavvy.cache.MemoryCache.Companion.cachedInMemory
import opensavvy.logger.Logger.Companion.trace
import opensavvy.logger.loggerFor
import opensavvy.state.Identifier
import opensavvy.state.Slice
import opensavvy.state.Slice.Companion.successful
import opensavvy.state.State
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * In-memory [Cache] implementation.
 *
 * Updates from the previous cache layer are stored in a dictionary.
 * When [get] is called, results are returned from the dictionary if available.
 * Otherwise, the query is transmitted to the previous layer.
 *
 * This implementation never frees the cache or invalidates elements inside it.
 * To free memory, add a subsequent layer responsible for it (e.g. [ExpirationCache]).
 *
 * Use the [cachedInMemory] factory for easy cache chaining:
 * ```kotlin
 * val cache = Cache.Default()
 *     .cachedInMemory()
 *     .expireAfter(2.minutes)
 * ```
 */
class MemoryCache<I : Identifier, T>(
	private val upstream: Cache<I, T>,
	context: CoroutineContext = EmptyCoroutineContext,
) : Cache<I, T> {

	private val log = loggerFor(this)

	/* Implementation explanation.
	 *
	 * Features:
	 * - the flow returned by 'get' is infinite (as long as downstream users are subscribed to it)
	 *   - if there is no cached value, a request is started and this flow becomes responsible for updating its values
	 *   - if there is a cached value, it is returned (there is NO expiration in this layer)
	 *   - in both cases, we subscribe to the internal cache and propagate changes to it
	 * - 'update' sets the cached value
	 * - 'expire' removed the cached value
	 */

	private val cache = HashMap<I, MutableStateFlow<Slice<T>?>>()
	private val cacheLock = Semaphore(1)

	private val jobs = HashMap<I, Job>()
	private val jobsLock = Semaphore(1)

	private val subscribeJob = SupervisorJob(context[Job])
	private val scope = CoroutineScope(subscribeJob)

	/** **UNSAFE**: only call when owning the [cacheLock] */
	private fun getUnsafe(id: I) = cache.getOrPut(id) { MutableStateFlow(null) }

	override fun get(id: I): State<T> = flow {
		val cached = cacheLock.withPermit { getUnsafe(id) }
			.onEach { slice ->
				if (slice == null) {
					// Now, someone should make a request to the previous layer.
					// However, multiple subscribers may see this event at the same time.
					// One of the subscribers becomes responsible for making the request

					jobsLock.withPermit {
						val job = jobs[id]
						if (job == null || !job.isActive) {
							// No one is currently making the request, I'm taking the responsibility to do it

							jobs[id] = scope.launch(CoroutineName("${this@MemoryCache} for $id")) {
								log.trace(id) { "Subscribing to the previous layer for" }

								val state = cacheLock.withPermit { getUnsafe(id) }

								upstream[id]
									.onEach { log.trace(it) { "Event for" } }
									.onEach { state.value = it }
									.collect()
							}
						}
					}
				}
			}
			.filterNotNull() // 'null' is an internal value, it shouldn't be returned to downstream users

		emitAll(cached)
	}

	override suspend fun update(values: Collection<Pair<I, T>>) {
		log.trace(values) { "updateAll" }

		jobsLock.withPermit {
			for ((id, _) in values) {
				jobs.remove(id)?.cancel("MemoryCache.expire(refs) was called")
			}
		}

		cacheLock.withPermit {
			for ((id, value) in values) {
				getUnsafe(id).value = successful(value)
			}
		}

		upstream.update(values)
	}

	override suspend fun expire(ids: Collection<I>) {
		log.trace(ids) { "expireAll" }

		jobsLock.withPermit {
			for (id in ids) {
				jobs.remove(id)?.cancel("MemoryCache.expire(refs) was called")
			}
		}

		cacheLock.withPermit {
			for (id in ids) {
				val cached = cache[id]

				if (cached == null) {
					log.trace(id) { "Could not find any cached value for the specified ID, this is a no-op." }
				} else if (cached.subscriptionCount.value == 0) {
					// No one cares about the value, we can free it
					cache.remove(id)
				} else {
					// At least one person is subscribed to the value, let's notify them that it's out-of-date
					cached.value = null
				}
			}
		}

		upstream.expire(ids)
	}

	override suspend fun expireAll() {
		log.trace { "expireAll" }

		jobsLock.withPermit {
			jobs.values.forEach { it.cancel("MemoryCache.expireAll() was called") }
			jobs.clear()
		}

		cacheLock.withPermit {
			val toRemove = ArrayList<I>()

			for ((id, cached) in cache) {
				if (cached.subscriptionCount.value == 0) {
					// No one cares about the value, we can free it
					toRemove += id
				} else {
					// At least one person is subscribed to the value, let's notify them that it's out-of-date
					cached.value = null
				}
			}

			for (id in toRemove)
				cache.remove(id)
		}

		upstream.expireAll()
	}

	companion object {
		fun <I : Identifier, T> Cache<I, T>.cachedInMemory(context: CoroutineContext) = MemoryCache(this, context)
	}
}
