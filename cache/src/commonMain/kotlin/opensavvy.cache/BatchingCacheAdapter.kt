package opensavvy.cache

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import opensavvy.state.*
import opensavvy.state.Slice.Companion.pending
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

/**
 * Cache implementation aimed to be the first link in a cache chain.
 *
 * This is not a valid implementation of a cache (it doesn't do any caching), and only serves as a link between caches
 * and the underlying network APIs.
 *
 * Unlike [CacheAdapter], this class is able to group requests together.
 */
class BatchingCacheAdapter<I : Identifier<T>, T>(
	context: CoroutineContext,
	/**
	 * The number of workers batching the requests.
	 *
	 * All requests are split among them.
	 * Each worker tries to batch as many requests as possible, then calls [queryBatch] at once.
	 *
	 * Increasing the number of workers may increase latency.
	 */
	workers: Int = 1,
	val queryBatch: (Set<I>) -> State<I, T>,
) : Cache<I, T> {

	private val requests: SendChannel<Pair<I, CompletableDeferred<StateFlow<Slice<I, T>>>>>

	init {
		require(workers > 0) { "There must be at least 1 worker: found $workers" }

		val requests = Channel<Pair<I, CompletableDeferred<StateFlow<Slice<I, T>>>>>()
		this.requests = requests

		val scope = CoroutineScope(context)
		repeat(workers) {
			scope.launch {
				worker(requests)
			}
		}
	}

	private suspend fun worker(requests: ReceiveChannel<Pair<I, CompletableDeferred<StateFlow<Slice<I, T>>>>>) {
		while (coroutineContext.isActive) {
			val batch = HashSet<I>()

			// Store the results
			// We have to store lists of Deferred in case multiple requests to the same Ref happen to be in the same
			// batch.
			val results = HashMap<I, MutableList<CompletableDeferred<StateFlow<Slice<I, T>>>>>()

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

			val states = HashMap<I, MutableStateFlow<Slice<I, T>>>()

			// Tell all clients that their request is starting
			for ((id, promises) in results) {
				val state = MutableStateFlow(pending(id, Progression.loading(0.0)))

				for (promise in promises) {
					promise.complete(state)
				}

				states[id] = state
			}

			// The channel is now empty, request all of them
			queryBatch(batch)
				.collect {
					val state = states[it.id]
						?: error("Could not find the state for the reference ${it.id}, this means we received an update for a reference we did not ask for.")

					state.value = it
				}
		}
	}

	override fun get(id: I): State<I, T> = state {
		val promise = CompletableDeferred<StateFlow<Slice<I, T>>>()

		requests.send(id to promise)

		emitAll(promise.await())
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
}
