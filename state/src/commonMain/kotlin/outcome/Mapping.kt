package opensavvy.state.outcome

/**
 * Replaces the value of this outcome if it is successful using [transform].
 *
 * If this outcome isn't successful, does nothing.
 */
inline fun <F, T, U> Outcome<F, T>.map(transform: (T) -> U) = when (this) {
	is Outcome.Failure -> this
	is Outcome.Success -> Outcome.Success(transform(this.value))
}

/**
 * Replaces the failure of this outcome if it is failed using [transformFailure].
 *
 * If this outcome isn't failed, does nothing.
 */
inline fun <F, T, G> Outcome<F, T>.mapFailure(transformFailure: (F) -> G) = when (this) {
	is Outcome.Failure -> Outcome.Failure(transformFailure(this.failure))
	is Outcome.Success -> this
}
