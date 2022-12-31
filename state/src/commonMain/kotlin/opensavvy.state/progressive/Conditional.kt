package opensavvy.state.progressive

import opensavvy.state.Progression
import opensavvy.state.progressive.ProgressiveSlice.*

/**
 * Executes [block] if this slice is [successful][Success].
 *
 * Otherwise, does nothing.
 */
inline fun <T> ProgressiveSlice<T>.onSuccess(block: (T) -> Unit) {
	if (this is Success<T>)
		block(this.value)
}

/**
 * Executes [block] if this slice is a [failure][Failure].
 *
 * Otherwise, does nothing.
 */
inline fun <T> ProgressiveSlice<T>.onFailure(block: (opensavvy.state.Failure) -> Unit) {
	if (this is Failure)
		block(this.failure)
}

/**
 * Executes [block] if this slice is loading (its [ProgressiveSlice.progress] is [Progression.Loading]).
 *
 * Note that this isn't synonymous with this slice being in the [Empty] state: successful or failed slices may
 * still be loading. For more information, see [ProgressiveSlice].
 *
 * Otherwise, does nothing.
 */
inline fun <T> ProgressiveSlice<T>.onLoading(block: (Progression.Loading) -> Unit) {
	val progression = progress

	if (progression is Progression.Loading)
		block(progression)
}
