package opensavvy.state.outcome

import opensavvy.state.failure.Failure

/**
 * Returns [Success.value][Outcome.Success.value], or `null` if this outcome is not successful.
 */
val <T : Any> Outcome<*, T>.valueOrNull: T?
    get() = (this as? Outcome.Success<T>)?.value

/**
 * Returns [Failure.failure][Outcome.Failure.failure], or `null` if this outcome is not a failure.
 */
val <F : Failure> Outcome<F, *>.failureOrNull: F?
    get() = (this as? Outcome.Failure)?.failure
