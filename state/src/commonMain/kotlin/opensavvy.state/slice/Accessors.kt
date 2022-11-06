package opensavvy.state.slice

import arrow.core.Either
import opensavvy.state.Failure

/**
 * Returns the value of this slice, or throws the [Failure] if it isn't successful.
 *
 * This function breaks the functional error handling paradigm, it shouldn't be used in regular code.
 * It is useful in tests.
 */
val <T> Slice<T>.valueOrThrow: T
	get() = when (this) {
		is Either.Left -> throw value.toException()
		is Either.Right -> value
	}
