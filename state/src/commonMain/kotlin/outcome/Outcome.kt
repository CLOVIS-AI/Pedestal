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
 *
 * ### Arrow
 *
 * Outcome is essentially identical to Arrow's Either. When using Arrow, we recommend using Either most of the time
 * because of all the convenience functions and DSLs it has. Using our companion library `state-arrow`, it is possible
 * to use Outcome in the Raise DSL.
 *
 * Because of this, we will keep Outcome as simple as possible, and avoid adding too much sugar.
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
