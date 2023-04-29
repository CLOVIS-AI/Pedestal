package opensavvy.cache

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.*
import opensavvy.logger.Logger.Companion.error
import opensavvy.logger.loggerFor
import opensavvy.state.coroutines.ProgressiveFlow
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.coroutines.coroutineContext

private typealias CacheStorage<F, T> = CompletableDeferred<StateFlow<ProgressiveOutcome<F, T>>>

/**
 * Cache implementation aimed to be the first link in a cache chain.
 *
 * This is not a valid implementation of a cache (it doesn't do any caching), and only serves as a link between caches
 * and the underlying network APIs.
 *
 * Unlike [CacheAdapter], this class is able to group requests together.
 */
class BatchingCacheAdapter<I, F, T>(
	scope: CoroutineScope,
	/**
	 * The number of workers batching the requests.
	 *
	 * All requests are split among them.
	 * Each worker tries to batch as many requests as possible, then calls [queryBatch] at once.
	 *
	 * Increasing the number of workers may increase latency.
	 */
	workers: Int = 1,
	val queryBatch: (Set<I>) -> Flow<Pair<I, ProgressiveOutcome<F, T>>>,
) : Cache<I, F, T> {

	private val log = loggerFor(this)

	private val requests: SendChannel<Pair<I, CacheStorage<F, T>>>

	init {
		require(workers > 0) { "There must be at least 1 worker: found $workers" }

		val requests = Channel<Pair<I, CacheStorage<F, T>>>()
		this.requests = requests

		repeat(workers) {
			scope.launch(CoroutineName("$this($it/$workers)")) {
				worker(requests)
			}
		}
	}

	private suspend fun worker(requests: ReceiveChannel<Pair<I, CacheStorage<F, T>>>) {
		while (coroutineContext.isActive) {
			val batch = HashSet<I>()

			// Store the results
			// We have to store lists of Deferred in case multiple requests to the same Ref happen to be in the same
			// batch.
			val results = HashMap<I, MutableList<CacheStorage<F, T>>>()

			run {
				// Suspend until a first request arrives
				val (ref, promise) = requests.receive()
				batch.add(ref)
				results.getOrPut(ref) { ArrayList() }
					.add(promise)
			}

			while (true) {
				// Try to read as many requests as possible, without suspending
				val (ref, promise) = requests.tryReceive()
					.getOrNull()
					?: break

				batch.add(ref)
				results.getOrPut(ref) { ArrayList() }
					.add(promise)
			}

			val states = HashMap<I, MutableStateFlow<ProgressiveOutcome<F, T>>>()

			// Tell all clients that their request is starting
			for ((id, promises) in results) {
				val state: MutableStateFlow<ProgressiveOutcome<F, T>> =
					MutableStateFlow(ProgressiveOutcome.Incomplete())

				for (promise in promises) {
					promise.complete(state)
				}

				states[id] = state
			}

			// The channel is now empty, request all of them
			queryBatch(batch)
				.collect { (id, it) ->
					val state = states[id]

					if (state != null)
						state.value = it
					else
						log.error(
							id,
							it
						) { "Could not find the state for the reference $id, this means we received an update for a reference we did not ask for. It has been ignored." }
				}
		}
	}

	override fun get(id: I): ProgressiveFlow<F, T> = flow {
		val promise = CompletableDeferred<StateFlow<ProgressiveOutcome<F, T>>>()

		requests.send(id to promise)

		emitAll(promise.await().filterNotNull())
	}

	override suspend fun update(values: Collection<Pair<I, T>>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expire(ids: Collection<I>) {
		// This cache layer has no state, nothing to do
	}

	override suspend fun expireAll() {
		// This cache layer has no state, nothing to do
	}

	companion object
}

/**
 * Creates a cache layer that batches cache requests and executes them at once.
 *
 * See [BatchingCacheAdapter].
 */
fun <I, F, T> batchingCache(
	scope: CoroutineScope,
	workers: Int = 1,
	transform: suspend FlowCollector<Pair<I, ProgressiveOutcome<F, T>>>.(Set<I>) -> Unit,
) = BatchingCacheAdapter<I, F, T>(scope, workers) { ids ->
	flow {
		transform(ids)
	}
}
