package opensavvy.state.outcome

import opensavvy.state.outcome.Outcome.Success

/**
 * Executes [block] if this outcome is [successful][Success].
 *
 * Otherwise, does nothing.
 */
inline fun <Value> Outcome<*, Value>.onSuccess(block: (Value) -> Unit) {
	if (this is Success<Value>)
		block(this.value)
}

/**
 * Executes [block] if this outcome is a [failure][Failure].
 *
 * Otherwise, does nothing.
 */
inline fun <Failure> Outcome<Failure, *>.onFailure(block: (Failure) -> Unit) {
	if (this is Outcome.Failure)
		block(this.failure)
}
