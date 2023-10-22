package opensavvy.state.outcome

/**
 * If this outcome is [successful][Outcome.Success], replaces its [value][Outcome.Success.value] using [transform].
 *
 * If this outcome isn't successful, does nothing.
 *
 * @see mapFailure Map the failure state instead of the success state.
 */
inline fun <Failure, InputValue, OutputValue> Outcome<Failure, InputValue>.map(transform: (InputValue) -> OutputValue) = when (this) {
	is Outcome.Failure -> this
	is Outcome.Success -> Outcome.Success(transform(this.value))
}

/**
 * If this outcome is [failed][Outcome.Failure], replaces its [failure][Outcome.Failure.failure] using [transformFailure].
 *
 * If this outcome isn't failed, does nothing.
 *
 * @see map Map the success state instead of the failure state.
 */
inline fun <InputFailure, Value, OutputFailure> Outcome<InputFailure, Value>.mapFailure(transformFailure: (InputFailure) -> OutputFailure) = when (this) {
	is Outcome.Failure -> Outcome.Failure(transformFailure(this.failure))
	is Outcome.Success -> this
}
