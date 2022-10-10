package opensavvy.state

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import opensavvy.state.Slice.Companion.failed
import opensavvy.state.Slice.Companion.pending
import opensavvy.state.Slice.Companion.successful
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Builder for [State].
 *
 * This type is used for conveniently building asynchronous flows in synchronous environments.
 * Instances are generally built using the [state] builder.
 */
typealias StateBuilder<I, T> = FlowCollector<Slice<I, T>>

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
fun <I : Identifier<T>, T> state(block: suspend StateBuilder<I, T>.() -> Unit) = flow(block)
	.catch {
		when (it) {
			// The user has requested the cancellation of the flow builder.
			// It is their responsibility to emit the correct exception in the flow, so we have nothing more to do.
			is StateBuilderCancellation -> Unit

			// The coroutine was cancelled.
			// CancellationExceptions MUST be rethrown, or it breaks coroutines
			// See https://kotlinlang.org/docs/cancellation-and-timeouts.html#cancellation-is-cooperative
			is CancellationException -> throw CancellationException("A state builder was cancelled", it)

			is Status.StandardFailure -> emitFailed(
				id = null,
				kind = it.kind,
				message = it.message ?: "Caught a downstream error",
				progression = Progression.done(),
				cause = it.cause
			)

			// All other exceptions are caught into the Kind.Unknown standard failure.
			else -> emitFailed(
				id = null,
				Status.StandardFailure.Kind.Unknown,
				"Unknown error caught in the state builder",
				progression = Progression.done(),
				cause = it
			)
		}
	}

/**
 * Exception used internally by the [state] function to provide cancellation functionality.
 */
class StateBuilderCancellation : RuntimeException("The 'state' builder has been cancelled")

//region Pending markers

/**
 * Emits a [pending] slice with the given [progression].
 */
suspend fun <I : Identifier<T>, T> StateBuilder<I, T>.emitPending(id: I?, progression: Progression.Loading) {
	emit(pending(id, progression))
}

/**
 * Emits a [pending] slice with an [Unquantified][Progression.Loading.Unquantified] progression.
 */
suspend fun <I : Identifier<T>, T> StateBuilder<I, T>.emitPending(id: I?) {
	emitPending(id, Progression.loading())
}

/**
 * Emits a [pending] slice with a [Quantified][Progression.Loading.Quantified] [progression].
 */
suspend fun <I : Identifier<T>, T> StateBuilder<I, T>.emitPending(id: I?, progression: Double) {
	emitPending(id, Progression.loading(progression))
}

//endregion
//region Successful markers

/**
 * Emits a [successful] slice with the given [value].
 */
suspend fun <I : Identifier<T>, T> StateBuilder<I, T>.emitSuccessful(
	id: I,
	value: T,
	progression: Progression = Progression.done(),
) {
	emit(successful(id, value, progression))
}

//endregion
//region Failed markers

/**
 * Emits a [failed] slice with the given [exception] and [message].
 */
suspend fun <I : Identifier<T>, T> StateBuilder<I, T>.emitFailed(
	id: I?,
	exception: RuntimeException,
	message: String,
	progression: Progression,
) {
	emit(failed(id, exception, message, progression))
}

/**
 * Emits a [failed] slice with the given [kind] and [message].
 */
suspend fun <I : Identifier<T>, T> StateBuilder<I, T>.emitFailed(
	id: I?,
	kind: Status.StandardFailure.Kind,
	message: String,
	progression: Progression,
	cause: Throwable? = null,
) {
	emit(failed(id, kind, message, cause, progression))
}

//endregion
//region Predicate checkers

/**
 * Ensures that [condition] is `true`.
 *
 * If it is, does nothing.
 * Otherwise, emits an [Invalid][Status.StandardFailure.Kind.Invalid] slice and cancels the [state] builder.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <I : Identifier<T>, T> StateBuilder<I, T>.ensureValid(
	id: I?,
	condition: Boolean,
	lazyMessage: () -> String,
) {
	contract {
		returns() implies condition
	}

	if (!condition) {
		emitFailed(id, Status.StandardFailure.Kind.Invalid, lazyMessage(), progression = Progression.done())
		throw StateBuilderCancellation()
	}
}

/**
 * Ensures that [condition] is `true`.
 *
 * If it is, does nothing.
 * Otherwise, emits an [Unauthenticated][Status.StandardFailure.Kind.Unauthenticated] slice and cancels the [state] builder.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <I : Identifier<T>, T> StateBuilder<I, T>.ensureAuthenticated(
	id: I?,
	condition: Boolean,
	lazyMessage: () -> String,
) {
	contract {
		returns() implies condition
	}

	if (!condition) {
		emitFailed(id, Status.StandardFailure.Kind.Unauthenticated, lazyMessage(), progression = Progression.done())
		throw StateBuilderCancellation()
	}
}

/**
 * Ensures that [condition] is `true`.
 *
 * If it is, does nothing.
 * Otherwise, emits an [Unauthorized][Status.StandardFailure.Kind.Unauthorized] slice and cancels the [state] builder.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <I : Identifier<T>, T> StateBuilder<I, T>.ensureAuthorized(
	id: I?,
	condition: Boolean,
	lazyMessage: () -> String,
) {
	contract {
		returns() implies condition
	}

	if (!condition) {
		emitFailed(id, Status.StandardFailure.Kind.Unauthorized, lazyMessage(), progression = Progression.done())
		throw StateBuilderCancellation()
	}
}

/**
 * Ensures that [condition] is `true`.
 *
 * If it is, does nothing.
 * Otherwise, emits an [NotFound][Status.StandardFailure.Kind.NotFound] slice and cancels the [state] builder.
 */
@OptIn(ExperimentalContracts::class)
suspend inline fun <I : Identifier<T>, T> StateBuilder<I, T>.ensureFound(
	id: I?,
	condition: Boolean,
	lazyMessage: () -> String,
) {
	contract {
		returns() implies condition
	}

	if (!condition) {
		emitFailed(id, Status.StandardFailure.Kind.NotFound, lazyMessage(), progression = Progression.done())
		throw StateBuilderCancellation()
	}
}

//endregion
