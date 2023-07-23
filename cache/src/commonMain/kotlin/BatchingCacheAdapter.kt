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
internal class BatchingCacheAdapter<I, F, T>(
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
 * Cache implementation which collects cache requests into batches which are queried at the same time.
 *
 * This adapter is meant to be used as the first layer in a layer chain. By itself, it does no caching (all calls to [get][Cache.get] call [transform]).
 * To learn more about layer chaining, or about the type parameters, see [Cache].
 *
 * ### Implementation
 *
 * > This describes the current implementation. It is possible for this to change in future versions.
 *
 * This adapter starts multiple [workers] which await requests. When one request arrives, the worker which receives it
 * attempts to assign to itself as many other requests as possible without suspending. Once it has collected the requests,
 * it calls [transform] with all the requests it is assigned to, and redistributes the results as appropriate.
 *
 * While a worker is sending a request, the other workers (if any) await for more requests.
 * In practice, this means a low number of workers increases the latency (as requests cannot start
 * until one of the workers is available). A high number of workers increases the likelihood of
 * two or more workers sharing a single batch between them, leading to smaller batches.
 *
 * ### Example
 *
 * ```kotlin
 * val scope: CoroutineScope = …
 *
 * // Let's imagine this is an I/O request
 * suspend fun foo(ids: Set<Int>): Map<Int, String> {
 *     println("Batch: $ids")
 *     delay(1000)
 *
 *     return ids.associateWith { it.toString() }
 * }
 *
 * val cachedFoo = batchingCache(scope) {
 *     val results = foo(it)
 *         .map { (id, value) -> id to value.success() }
 *
 *     for ((id, result) in results) {
 *         emit(id to result)
 *     }
 * }
 *
 * scope.launch { println(cachedFoo[5].now()) }
 * scope.launch { println(cachedFoo[6].now()) }
 * scope.launch { println(cachedFoo[7].now()) }
 *
 * /* Output:
 *  *
 *  * Batch: [5, 6, 7]
 *  * Success(value=5)
 *  * Success(value=6)
 *  * Success(value=7)
 *  */
 * ```
 *
 * @param scope The coroutine scope in which the workers are started.
 * @param workers The number of parallel workers which should be started (at least 1).
 */
fun <I, F, T> batchingCache(
	scope: CoroutineScope,
	workers: Int = 1,
	transform: suspend FlowCollector<Pair<I, ProgressiveOutcome<F, T>>>.(Set<I>) -> Unit,
): Cache<I, F, T> = BatchingCacheAdapter(scope, workers) { ids ->
	flow {
		transform(ids)
	}
}
