package opensavvy.state.outcome

import opensavvy.state.outcome.Outcome.Failure
import opensavvy.state.outcome.Outcome.Success
import opensavvy.state.failure.Failure as FailureSupertype


/**
 * Executes [block] if this outcome is [successful][Success].
 *
 * Otherwise, does nothing.
 */
inline fun <T> Outcome<*, T>.onSuccess(block: (T) -> Unit) {
    if (this is Success<T>)
        block(this.value)
}

/**
 * Executes [block] if this outcome is a [failure][Failure].
 *
 * Otherwise, does nothing.
 */
inline fun <F : FailureSupertype> Outcome<F, *>.onFailure(block: (F) -> Unit) {
    if (this is Failure)
        block(this.failure)
}
