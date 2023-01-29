package opensavvy.state.progressive

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import opensavvy.state.Progression
import opensavvy.state.progressive.ProgressiveOutcome.*

//region Actions

/**
 * Executes [block] if this outcome is [successful][Success].
 *
 * Otherwise, does nothing.
 */
inline fun <T> ProgressiveOutcome<T>.onSuccess(block: (T) -> Unit) {
	if (this is Success<T>)
		block(this.value)
}

/**
 * Executes [block] if this outcome is a [failure][Failure].
 *
 * Otherwise, does nothing.
 */
inline fun <T> ProgressiveOutcome<T>.onFailure(block: (opensavvy.state.Failure) -> Unit) {
	if (this is Failure)
		block(this.failure)
}

/**
 * Executes [block] if this outcome is loading (its [ProgressiveOutcome.progress] is [Progression.Loading]).
 *
 * Note that this isn't synonymous with this outcome being in the [Empty] state: successful or failed outcomes may
 * still be loading. For more information, see [ProgressiveOutcome].
 *
 * Otherwise, does nothing.
 */
inline fun <T> ProgressiveOutcome<T>.onLoading(block: (Progression.Loading) -> Unit) {
	val progression = progress

	if (progression is Progression.Loading)
		block(progression)
}

//endregion
//region Combinations

/**
 * Replaces the value of this outcome if it is successful using [transform].
 *
 * If this outcome isn't successful, does nothing.
 */
inline fun <T, U> ProgressiveOutcome<T>.map(transform: (T) -> U) = when (this) {
	is Empty -> this
	is Failure -> this
	is Success -> Success(transform(this.value), this.progress)
}

/**
 * Replaces the successful values using [transform].
 *
 * Values that are not successful are unchanged.
 */
inline fun <T, U> Flow<ProgressiveOutcome<T>>.mapSuccess(crossinline transform: (T) -> U) = this
	.map { it.map(transform) }

//endregion
