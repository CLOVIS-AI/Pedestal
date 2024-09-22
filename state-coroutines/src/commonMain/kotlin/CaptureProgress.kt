package opensavvy.state.coroutines

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import opensavvy.logger.Logger.Companion.warn
import opensavvy.logger.loggerFor
import opensavvy.progress.Progress
import opensavvy.progress.coroutines.CoroutineProgressReporter
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.withProgress

private fun <Failure, Value> ProducerScope<ProgressiveOutcome<Failure, Value>>.progressExtractor() = CoroutineProgressReporter {
	if (it is Progress.Loading) {
		val result = trySend(ProgressiveOutcome.Incomplete(it))

		if (result.isFailure) {
			loggerFor(this).warn(
				it,
				result
			) { "Could not send the progression event to the channel, it has been ignored" }
		}
	}
	// else: the action has reported that it's over, but the results haven't reached us yet, they'll probably
	//       arrive just afterward
}

/**
 * Captures the progress information of the current operation using [CoroutineProgressReporter].
 *
 * Because flows cannot emit from multiple coroutines, the implementation of this function requires the usage
 * of channels, which are more expensive.
 * If possible, prefer using the [failed] builder.
 */
// TODO in #145: Rewrite on top of Progress' captureProgress
@Suppress("RemoveExplicitTypeArguments") // IDEA bug, they are necessary here
fun <Failure, Value> Flow<Outcome<Failure, Value>>.captureProgress(): Flow<ProgressiveOutcome<Failure, Value>> = channelFlow {
	this@captureProgress
		.flowOn(progressExtractor<Failure, Value>())
		.map { it.withProgress() }
		.onEach { send(it) }
		.collect()
}.buffer(1)

/**
 * Captures the progress information of [block] using [CoroutineProgressReporter].
 *
 * Because flows cannot emit from multiple coroutines, the implementation of this function requires the usage
 * of channels, which are more expensive.
 * If possible, prefer using the [failed] builder.
 */
// TODO in #145: Rewrite on top of Progress' captureProgress
fun <Failure, Value> captureProgress(block: suspend () -> Outcome<Failure, Value>): Flow<ProgressiveOutcome<Failure, Value>> = channelFlow {
	withContext(progressExtractor()) {
		send(block().withProgress())
	}
}
