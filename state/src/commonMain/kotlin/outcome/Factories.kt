package opensavvy.state.outcome

import arrow.core.left
import arrow.core.right
import opensavvy.state.Failure

fun <T> successful(value: T): Outcome<T> =
	value.right()

fun failed(
	message: String,
	kind: Failure.Kind = Failure.Kind.Unknown,
	cause: RuntimeException? = null,
): Outcome<Nothing> =
	Failure(kind, message, cause).left()
