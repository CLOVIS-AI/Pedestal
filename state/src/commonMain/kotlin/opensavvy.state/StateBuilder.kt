package opensavvy.state

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import opensavvy.state.Slice.Companion.failed
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Builder for [State].
 *
 * This type is used for conveniently building asynchronous flows in synchronous environments.
 * Instances are generally built using the [state] builder.
 */
typealias StateBuilder<T> = FlowCollector<Slice<T>>

/**
 * Builder for [State].
 *
 * ```kotlin
 * val id = …
 * val foo = state {
 *     emitLoading(id, 50.0) // 50%
 *     ensureValid(id, currentUser.isAllowedToRead(id)) { "You are not allowed to read the object $id" }
 *     emitSuccessful(id, database.dereference(id))
 * }
 * ```
 */
fun <T> state(block: suspend StateBuilder<T>.() -> Unit) = flow(block)
	.catch {
		when (it) {
			// The user has requested the cancellation of the flow builder.
			// It is their responsibility to emit the correct exception in the flow, so we have nothing more to do.
			is StateBuilderCancellation -> Unit

			// The coroutine was cancelled.
			// CancellationExceptions MUST be rethrown, or it breaks coroutines
			// See https://kotlinlang.org/docs/cancellation-and-timeouts.html#cancellation-is-cooperative
			is CancellationException -> throw CancellationException("A state builder was cancelled", it)

			is Status.StandardFailure -> emit(
				failed(
					it.kind,
					it.message ?: "Caught a downstream error",
					it.cause,
					Progression.done()
				)
			)

			// All other exceptions are caught into the Kind.Unknown standard failure.
			else -> emit(
				failed(
					Status.StandardFailure.Kind.Unknown,
					"Unknown error caught in the state builder",
					it,
					Progression.done()
				)
			)
		}
	}

/**
 * Exception used internally by the [state] function to provide cancellation functionality.
 */
private class StateBuilderCancellation : RuntimeException("The 'state' builder has been cancelled")

//region Predicate checkers

/**
 * Stops the currently running [state] builder.
 *
 * If no [state] builder is active, acts as a [CancellationException].
 */
@Suppress("UnusedReceiverParameter") // used for namespacing
fun <T> StateBuilder<T>.cancel(): Nothing {
	throw StateBuilderCancellation()
}

/**
 * Ensures that [condition] is `true`.
 *
 * If it is, does nothing.
 * Otherwise, emits an [Invalid][Status.StandardFailure.Kind.Invalid] slice and cancels the [state] builder.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T> StateBuilder<T>.ensureValid(
	condition: Boolean,
	lazyMessage: () -> String,
) {
	contract {
		returns() implies condition
	}

	if (!condition) {
		emit(failed(Status.StandardFailure.Kind.Invalid, lazyMessage(), progression = Progression.done()))
		cancel()
	}
}

/**
 * Ensures that [condition] is `true`.
 *
 * If it is, does nothing.
 * Otherwise, emits an [Unauthenticated][Status.StandardFailure.Kind.Unauthenticated] slice and cancels the [state] builder.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T> StateBuilder<T>.ensureAuthenticated(
	condition: Boolean,
	lazyMessage: () -> String,
) {
	contract {
		returns() implies condition
	}

	if (!condition) {
		emit(failed(Status.StandardFailure.Kind.Unauthenticated, lazyMessage(), progression = Progression.done()))
		cancel()
	}
}

/**
 * Ensures that [condition] is `true`.
 *
 * If it is, does nothing.
 * Otherwise, emits an [Unauthorized][Status.StandardFailure.Kind.Unauthorized] slice and cancels the [state] builder.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T> StateBuilder<T>.ensureAuthorized(
	condition: Boolean,
	lazyMessage: () -> String,
) {
	contract {
		returns() implies condition
	}

	if (!condition) {
		emit(failed(Status.StandardFailure.Kind.Unauthorized, lazyMessage(), progression = Progression.done()))
		cancel()
	}
}

/**
 * Ensures that [condition] is `true`.
 *
 * If it is, does nothing.
 * Otherwise, emits an [NotFound][Status.StandardFailure.Kind.NotFound] slice and cancels the [state] builder.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <T> StateBuilder<T>.ensureFound(
	condition: Boolean,
	lazyMessage: () -> String,
) {
	contract {
		returns() implies condition
	}

	if (!condition) {
		emit(failed(Status.StandardFailure.Kind.NotFound, lazyMessage(), progression = Progression.done()))
		cancel()
	}
}

//endregion
