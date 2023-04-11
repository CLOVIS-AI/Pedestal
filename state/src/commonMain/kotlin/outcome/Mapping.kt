package opensavvy.state.outcome

import opensavvy.state.failure.Failure

/**
 * Replaces the value of this outcome if it is successful using [transform].
 *
 * If this outcome isn't successful, does nothing.
 */
inline fun <F : Failure, T, U> Outcome<F, T>.map(transform: (T) -> U) = when (this) {
    is Outcome.Failure -> this
    is Outcome.Success -> Outcome.Success(transform(this.value))
}
