package opensavvy.backbone.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import opensavvy.backbone.Cache
import opensavvy.backbone.Data
import opensavvy.backbone.Data.Companion.initialData
import opensavvy.backbone.Ref
import opensavvy.backbone.cache.MemoryCache.Companion.cachedInMemory
import opensavvy.logger.Logger.Companion.trace
import opensavvy.logger.loggerFor
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
class MemoryCache<O>(
	upstream: Cache<O>,
	context: CoroutineContext = EmptyCoroutineContext,
) : Cache.CacheLayer<O>(upstream) {

	private val cache = HashMap<Ref<O>, MutableStateFlow<Data<O>>>()
	private val cacheLock = Semaphore(1)

	private val jobs = HashMap<Ref<O>, Job>()
	private val jobsLock = Semaphore(1)

	private val subscribeJob = SupervisorJob(context[Job])
	private val scope = CoroutineScope(subscribeJob)

	private fun getUnsafe(ref: Ref<O>) = cache.getOrPut(ref) { MutableStateFlow(ref.initialData) }

	override fun get(ref: Ref<O>): Flow<Data<O>> = flow {
		log.trace(ref) { "get called for" }

		val cached = cacheLock.withPermit { getUnsafe(ref) }

		cached.collect { data ->
			emit(data)

			jobsLock.withPermit {
				// A new event arrived
				// Possible causes:
				// - the previous layer was updated
				// - 'update' or 'expire' were called

				// There are three possible cases:
				// 1. We are not subscribed to the previous layer for this ref
				//    -> subscribe to it
				// 2. We are subscribed to the previous layer for this ref
				//    -> nothing to do
				// 3. We were previously subscribed, but the subscriber died
				//    -> subscribe to the ref (overwrite the dead subscriber)

				val job = jobs[ref]
				if (job == null || !job.isActive) {
					// job == null: case 1
					// job is not active: case 3
					// in both cases, a new job must be started

					jobs[ref] = scope.launch(CoroutineName("MemoryCache for $ref")) {
						log.trace { "Subscribing to the previous layer for $ref" }

						upstream[ref]
							.collect { cached.value = it }
					}
				} // else: case 2, nothing to do
			}
		}
	}.distinctUntilChanged()
		.onEach { log.trace(it) { "new value emitted from 'get'" } }

	override suspend fun updateAll(values: Iterable<Data<O>>) {
		log.trace(values) { "updateAll" }

		cacheLock.withPermit {
			for (value in values)
				getUnsafe(value.ref).value = value
		}

		super.updateAll(values)
	}

	override suspend fun expireAll(refs: Iterable<Ref<O>>) {
		log.trace(refs) { "expireAll" }

		jobsLock.withPermit {
			for (ref in refs) {
				jobs.remove(ref)?.cancel("MemoryCache.expireAll(refs) was called")
			}
		}

		cacheLock.withPermit {
			for (ref in refs)
				cache[ref]?.value = ref.initialData
		}
	}

	override suspend fun expireAll() {
		log.trace { "expireAll" }

		jobsLock.withPermit {
			jobs.values.forEach { it.cancel("MemoryCache.expireAll() was called") }
		}

		cacheLock.withPermit {
			for (ref in cache.keys)
				cache[ref]?.value = ref.initialData
		}
	}

	companion object {
		private val log = loggerFor(this)

		fun <O> Cache<O>.cachedInMemory(context: CoroutineContext = EmptyCoroutineContext) = MemoryCache(this, context)
	}
}
