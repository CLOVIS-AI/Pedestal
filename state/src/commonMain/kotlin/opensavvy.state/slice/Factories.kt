package opensavvy.state.slice

import arrow.core.left
import arrow.core.right
import opensavvy.state.Failure

fun <T> successful(value: T) =
	value.right()

fun failed(message: String, kind: Failure.Kind = Failure.Kind.Unknown, cause: RuntimeException? = null) =
	Failure(kind, message, cause).left()
