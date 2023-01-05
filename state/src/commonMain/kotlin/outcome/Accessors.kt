package opensavvy.state.outcome

import arrow.core.Either
import opensavvy.state.Failure

/**
 * Returns the value of this outcome, or throws the [Failure] if it isn't successful.
 *
 * This function breaks the functional error handling paradigm, it shouldn't be used in regular code.
 * It is useful in tests.
 */
fun <T> Outcome<T>.orThrow(): T = when (this) {
	is Either.Left -> throw value.toException()
	is Either.Right -> value
}
