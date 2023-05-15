package opensavvy.state.progressive

/**
 * If this outcome is [successful][ProgressiveOutcome.Success], replaces its [value][ProgressiveOutcome.Success.value] using [transform].
 *
 * If this outcome isn't successful, does nothing.
 *
 * @see mapFailure Map the failure state instead of the success state.
 */
inline fun <F, T, U> ProgressiveOutcome<F, T>.map(transform: (T) -> U) = when (this) {
    is ProgressiveOutcome.Incomplete -> this
    is ProgressiveOutcome.Failure -> this
    is ProgressiveOutcome.Success -> ProgressiveOutcome.Success(transform(this.value), this.progress)
}

/**
 * If this outcome is [failed][ProgressiveOutcome.Failure], replaces its [failure][ProgressiveOutcome.Failure.failure] using [transformFailure].
 *
 * If this outcome isn't failed, does nothing.
 *
 * @see map Map the success state instead of the failure state.
 */
inline fun <F, T, G> ProgressiveOutcome<F, T>.mapFailure(transformFailure: (F) -> G) = when (this) {
    is ProgressiveOutcome.Incomplete -> this
    is ProgressiveOutcome.Failure -> ProgressiveOutcome.Failure(transformFailure(this.failure), this.progress)
    is ProgressiveOutcome.Success -> this
}
