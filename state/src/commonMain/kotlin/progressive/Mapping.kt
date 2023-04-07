package opensavvy.state.progressive

import opensavvy.state.failure.Failure

/**
 * Replaces the value of this outcome if it is successful using [transform].
 *
 * If this outcome isn't successful, does nothing.
 */
inline fun <F : Failure, T, U> ProgressiveOutcome<F, T>.map(transform: (T) -> U) = when (this) {
    is ProgressiveOutcome.Incomplete -> this
    is ProgressiveOutcome.Failure -> this
    is ProgressiveOutcome.Success -> ProgressiveOutcome.Success(transform(this.value), this.progress)
}
