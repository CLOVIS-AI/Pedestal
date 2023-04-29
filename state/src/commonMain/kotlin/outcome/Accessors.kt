package opensavvy.state.outcome

import opensavvy.state.outcome.Outcome.Failure
import opensavvy.state.outcome.Outcome.Success

// region Get or null

/**
 * Returns [Success.value], or `null` if this outcome is not successful.
 */
val <T : Any> Outcome<*, T>.valueOrNull: T?
    get() = (this as? Success)?.value

/**
 * Returns [Failure.failure], or `null` if this outcome is not a failure.
 */
val <F : Any> Outcome<F, *>.failureOrNull: F?
    get() = (this as? Failure)?.failure

// endregion
// region Safe get via Nothing

/**
 * Returns [Success.value].
 */
val <T> Outcome<Nothing, T>.value: T
    // This cast is safe, because a Failure of Nothing is impossible
    get() = (this as Success).value

/**
 * Returns [Failure.failure].
 */
val <F> Outcome<F, Nothing>.failure: F
    // This cast is safe, because a Success of Nothing is impossible
    get() = (this as Failure).failure

// endregion
