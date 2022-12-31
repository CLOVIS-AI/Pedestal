package opensavvy.state.progressive

import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import opensavvy.state.Progression
import opensavvy.state.ProgressionReporter
import opensavvy.state.slice.Slice

private val ProducerScope<ProgressiveSlice<Nothing>>.progressExtractor: ProgressionReporter
	get() = ProgressionReporter.callbackReporter {
		if (it is Progression.Loading)
			send(ProgressiveSlice.Empty(it))
		// else: the action has reported that it's over, but the results haven't reached us yet, they'll probably
		//       arrive just afterwards
	}

/**
 * Captures the progress information of the current operation using [ProgressionReporter].
 *
 * Because flows cannot emit from multiple coroutines, the implementation of this function requires the usage
 * of channels, which are more expensive.
 * If possible, prefer using the [progressiveSlice] builder.
 */
fun <T> Flow<Slice<T>>.captureProgress() = channelFlow {
	this@captureProgress
		.flowOn(progressExtractor)
		.map { it.withProgress() }
		.onEach { send(it) }
		.collect()
}.buffer(1)

/**
 * Captures the progress information of [block] using [ProgressionReporter].
 *
 * Because flows cannot emit from multiple coroutines, the implementation of this function requires the usage
 * of channels, which are more expensive.
 * If possible, prefer using the [progressiveSlice] builder.
 */
fun <T> captureProgress(block: suspend () -> Slice<T>) = channelFlow {
	withContext(progressExtractor) {
		send(block().withProgress())
	}
}
