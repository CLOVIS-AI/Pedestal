package opensavvy.state.slice

import arrow.core.continuations.EffectScope
import arrow.core.continuations.either
import arrow.core.left
import opensavvy.state.Failure

/**
 * Performs some calculation which may fail.
 */
suspend fun <T> slice(block: suspend EffectScope<Failure>.() -> T): Slice<T> =
	try {
		either(block)
	} catch (e: Failure.FailureException) {
		e.failure.left()
	} catch (e: IllegalArgumentException) {
		failed(e.message ?: "IllegalArgumentException without a message", Failure.Kind.Invalid, cause = e)
	} catch (e: IllegalStateException) {
		failed(e.message ?: "IllegalStateException without a message", Failure.Kind.Invalid, cause = e)
	} catch (e: NoSuchElementException) {
		failed(e.message ?: "NoSuchElementException without a message", Failure.Kind.NotFound, cause = e)
	}
