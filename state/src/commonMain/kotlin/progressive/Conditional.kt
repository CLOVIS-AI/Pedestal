package opensavvy.state.progressive

import opensavvy.progress.Progress
import opensavvy.state.progressive.ProgressiveOutcome.*

/**
 * Executes [block] if this outcome is [successful][Success].
 *
 * Otherwise, does nothing.
 */
inline fun <T> ProgressiveOutcome<*, T>.onSuccess(block: (T) -> Unit) {
	if (this is Success<T>)
		block(this.value)
}

/**
 * Executes [block] if this outcome is a [failure][Failure].
 *
 * Otherwise, does nothing.
 */
inline fun <F> ProgressiveOutcome<F, *>.onFailure(block: (F) -> Unit) {
	if (this is Failure)
		block(this.failure)
}

/**
 * Executes [block] if this outcome is [incomplete][Incomplete].
 *
 * Otherwise, does nothing.
 */
inline fun ProgressiveOutcome<*, *>.onIncomplete(block: () -> Unit) {
	if (this is Incomplete)
		block()
}

/**
 * Executes [block] if this outcome is loading (its [ProgressiveOutcome.progress] is [Progress.Loading]).
 *
 * Note that this isn't synonymous with this outcome being in the [Incomplete] state: successful or failed outcomes may
 * still be loading. For more information, see [ProgressiveOutcome].
 *
 * Otherwise, does nothing.
 */
inline fun ProgressiveOutcome<*, *>.onLoading(block: (Progress.Loading) -> Unit) {
	val progression = progress

	if (progression is Progress.Loading)
		block(progression)
}
