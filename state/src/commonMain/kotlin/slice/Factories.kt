package opensavvy.state.slice

import arrow.core.left
import arrow.core.right
import opensavvy.state.Failure

fun <T> successful(value: T): Slice<T> =
	value.right()

fun failed(
	message: String,
	kind: Failure.Kind = Failure.Kind.Unknown,
	cause: RuntimeException? = null,
): Slice<Nothing> =
	Failure(kind, message, cause).left()
