package opensavvy.state.outcome

import opensavvy.state.outcome.Outcome.Failure
import opensavvy.state.outcome.Outcome.Success
import opensavvy.state.progressive.ProgressiveOutcome

/**
 * The result of an operation.
 *
 * To store progress information as well as the result of the operation, please see [ProgressiveOutcome].
 *
 * There are two possible cases:
 * - [Success] if a successful result is available (see [Success.value]),
 * - [Failure] if a failed result is available (see [Failure.failure]).
 *
 * To create outcomes from computations, use the [success] and [failed] factories.
 */
sealed class Outcome<out F, out T> {

    /**
     * The latest known result of the operation was a success, available as [value].
     */
    data class Success<T>(
        val value: T,
    ) : Outcome<Nothing, T>()

    /**
     * The latest known result of the operation was a failure, available as [failure].
     */
    data class Failure<F>(
        val failure: F,
    ) : Outcome<F, Nothing>()
}
