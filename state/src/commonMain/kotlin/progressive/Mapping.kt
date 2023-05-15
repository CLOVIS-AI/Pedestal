package opensavvy.state.progressive

/**
 * Replaces the value of this outcome if it is successful using [transform].
 *
 * If this outcome isn't successful, does nothing.
 */
inline fun <F, T, U> ProgressiveOutcome<F, T>.map(transform: (T) -> U) = when (this) {
    is ProgressiveOutcome.Incomplete -> this
    is ProgressiveOutcome.Failure -> this
    is ProgressiveOutcome.Success -> ProgressiveOutcome.Success(transform(this.value), this.progress)
}

/**
 * Replaces the value of this outcome if it is failed using [transformFailure].
 *
 * If this outcome isn't failed, does nothing.
 */
inline fun <F, T, G> ProgressiveOutcome<F, T>.mapFailure(transformFailure: (F) -> G) = when (this) {
    is ProgressiveOutcome.Incomplete -> this
    is ProgressiveOutcome.Failure -> ProgressiveOutcome.Failure(transformFailure(this.failure), this.progress)
    is ProgressiveOutcome.Success -> this
}
