package opensavvy.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import opensavvy.logger.Logger.Companion.trace
import opensavvy.logger.loggerFor
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.failure.Failure
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.copy

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
 * Use the [cachedInMemory] factory for easy cache chaining.
 */
class MemoryCache<I, F : Failure, T>(
	private val upstream: Cache<I, F, T>,
	private val job: Job = SupervisorJob(),
) : Cache<I, F, T> {

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

	private val cache = HashMap<I, MutableStateFlow<ProgressiveOutcome<F, T>?>>()
	private val cacheLock = Mutex()

	private val jobs = HashMap<I, Job>()
	private val jobsLock = Mutex()

	/** **UNSAFE**: only call when owning the [cacheLock] */
	private fun getUnsafe(id: I) = cache.getOrPut(id) { MutableStateFlow(null) }

	override fun get(id: I): ProgressiveFlow<F, T> = flow {
		val cached = cacheLock.withLock("get($id)") { getUnsafe(id) }
			.onEach { out ->
				if (out == null) {
					// Now, someone should make a request to the previous layer.
					// However, multiple subscribers may see this event at the same time.
					// One of the subscribers becomes responsible for making the request

					attemptTakeResponsibilityPingUpstream(id)
				}
			}
			.filterNotNull() // 'null' is an internal value, it shouldn't be returned to downstream users

		emitAll(cached)
	}.onEach { log.trace(it) { "Emit value for $id ->" } }

	private suspend fun attemptTakeResponsibilityPingUpstream(id: I) {
		jobsLock.withLock("takeResponsibility($id)") {
			val job = jobs[id]
			if (job == null || !job.isActive) {
				// No one is currently making the request, I'm taking the responsibility to do it

				val childContext = currentCoroutineContext() +
					CoroutineName("$this(for = $id)") +
					this.job

				jobs[id] = CoroutineScope(childContext).launch {
					log.trace(id) { "Subscribing to the previous layer for" }

					val state = cacheLock.withLock("upstreamSubscription($id)") { getUnsafe(id) }

					upstream[id]
						.onEach { log.trace(it) { "Prev value for $id ->" } }
						.map {
							val previousValue = state.value

							// If the previous cache layer says it's a new value, but we remember what the previous
							// result was, we return the previous value with the new progress information
							if (it is ProgressiveOutcome.Incomplete && previousValue != null)
								previousValue.copy(progress = it.progress)
							else
								it
						}
						.onEach { state.value = it }
						.collect()
				}
			} // else: someone else has taken the responsibility to start the request, I don't need to do anything
		}
	}

	override suspend fun update(values: Collection<Pair<I, T>>) {
		log.trace(values) { "update" }

		jobsLock.withLock("updateJobs($values)") {
			for ((id, _) in values) {
				jobs.remove(id)?.cancel("MemoryCache.expire(refs) was called")
			}
		}

		cacheLock.withLock("updateCache($values)") {
			for ((id, value) in values) {
				getUnsafe(id).value = ProgressiveOutcome.Success(value)
			}
		}

		upstream.update(values)
	}

	override suspend fun expire(ids: Collection<I>) {
		log.trace(ids) { "expire" }

		jobsLock.withLock("expireJobs($ids)") {
			for (id in ids) {
				jobs.remove(id)?.cancel("MemoryCache.expire(refs) was called")
			}
		}

		cacheLock.withLock("expireCache($ids)") {
			for (id in ids) {
				val cached = cache[id]

				if (cached == null) {
					log.trace(id) { "Could not find any cached value for the specified ID, this is a no-op." }
				} else if (cached.subscriptionCount.value == 0) {
					// No one cares about the value, we can free it
					cache.remove(id)
				} else {
					// At least one person is subscribed to the value, but we just cancelled ongoing requests,
					// let's start a new one
					attemptTakeResponsibilityPingUpstream(id)
				}
			}
		}

		upstream.expire(ids)
	}

	override suspend fun expireAll() {
		log.trace { "expireAll" }

		jobsLock.withLock("expireAllJobs()") {
			jobs.values.forEach { it.cancel("MemoryCache.expireAll() was called") }
			jobs.clear()
		}

		cacheLock.withLock("expireAllCaches()") {
			val toRemove = ArrayList<I>()

			for ((id, cached) in cache) {
				if (cached.subscriptionCount.value == 0) {
					// No one cares about the value, we can free it
					toRemove += id
				} else {
					// At least one person is subscribed to the value, but we just cancelled ongoing requests,
					// let's start a new one
					attemptTakeResponsibilityPingUpstream(id)
				}
			}

			for (id in toRemove)
				cache.remove(id)
		}

		upstream.expireAll()
	}

	/**
	 * Goes through the entire cache and expires all values for which [predicate] returns `true`.
	 */
	internal suspend fun expireIf(predicate: (I) -> Boolean) {
		log.trace { "expireIf" }

		val targets = cacheLock.withLock("expireIf($predicate)") {
			cache.keys.filter(predicate)
		}

		expire(targets)
	}

	companion object
}

/**
 * Creates a new cache layer which stores the last queried value for each identifier, and joins requests such that
 * multiple subscribers to the same value only start a single request.
 *
 * For more information, see [MemoryCache].
 */
fun <I, F : Failure, T> Cache<I, F, T>.cachedInMemory(job: Job) = MemoryCache(this, job)
