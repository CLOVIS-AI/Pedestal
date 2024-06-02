package opensavvy.progress.coroutines

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import opensavvy.logger.Logger.Companion.warn
import opensavvy.logger.loggerFor
import opensavvy.progress.ExperimentalProgressApi
import opensavvy.progress.Progress
import opensavvy.progress.Progressive
import opensavvy.progress.done

// region Capture progress events

@ExperimentalProgressApi
private fun <Value> sendProgressTo(receiver: ProducerScope<Progressive<Value?>>) = CoroutineProgressReporter {
	if (it is Progress.Loading) {
		val result = receiver.trySend(Progressive(null, it))

		if (result.isFailure) {
			loggerFor(receiver).warn(it, result) { "Could not send the progression event $it to the channel, it has been ignored" }
		}
	}
	// else: the action has reported that it's over, but the results haven't reached us yet, they'll probably
	//       arrive just afterward
}

/**
 * Captures calls to [report][opensavvy.progress.coroutines.report] within the given flow and reports them as a new [Flow].
 *
 * ### Emissions
 *
 * Each [report][opensavvy.progress.coroutines.report] call inside the initial flow leads to a new emitted value in the downstream
 * flow, with a [Progressive.value] of `null`.
 * All emissions in the initial flow leads to a new emitted value in the downstream flow, with the emission as [Progressive.value]
 * and a [Progressive.progress] of [done].
 */
@ExperimentalProgressApi
fun <Value> Flow<Value>.captureProgress(): Flow<Progressive<Value?>> = channelFlow {
	this@captureProgress
		.flowOn(sendProgressTo<Value>(this))
		.map { Progressive(it, done()) }
		.onEach { send(it) }
		.collect()
}.buffer(1)

/**
 * Captures calls to [report][opensavvy.progress.coroutines.report] within [block] and reports them as a [Flow].
 *
 * ### Example
 *
 * ```kotlin
 * captureProgress {
 *     report(loading(0.1))
 *     delay(100)
 *     report(loading(0.9))
 *     "It's over"
 * }
 * ```
 * generates the flow containing the elements:
 * ```
 * null Loading(10%)
 * null Loading(90%)
 * "It's over" Done
 * ```
 *
 * ### Termination
 *
 * The returned [Flow] terminates right after [block] terminates, and the returned value is assumed to be the final expected value.
 */
@ExperimentalProgressApi
fun <Value> captureProgress(block: suspend () -> Value): Flow<Progressive<Value?>> = channelFlow {
	withContext(sendProgressTo(this)) {
		send(Progressive(block(), done()))
	}
}

// endregion
