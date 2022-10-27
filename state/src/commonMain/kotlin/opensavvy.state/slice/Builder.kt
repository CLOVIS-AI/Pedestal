package opensavvy.state.slice

import arrow.core.continuations.EffectScope
import arrow.core.continuations.either
import arrow.core.left
import kotlinx.coroutines.withContext
import opensavvy.state.Failure
import opensavvy.state.ProgressionReporter
import kotlin.coroutines.coroutineContext

/**
 * Performs some calculation which may fail.
 */
suspend fun <T> slice(reporter: ProgressionReporter? = null, block: suspend EffectScope<Failure>.() -> T): Slice<T> =
	withContext(reporter ?: coroutineContext) {
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
	}
