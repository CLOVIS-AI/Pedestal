package opensavvy.state.slice

import arrow.core.continuations.EffectScope
import opensavvy.state.Failure
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
suspend inline fun EffectScope<Failure>.ensureOrShift(
	condition: Boolean,
	kind: Failure.Kind = Failure.Kind.Unknown,
	lazyMessage: () -> String,
) {
	contract {
		returns() implies condition
		callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
	}

	if (!condition)
		shift<Unit>(Failure(kind, lazyMessage()))
}

@OptIn(ExperimentalContracts::class)
suspend inline fun EffectScope<Failure>.ensureValid(condition: Boolean, lazyMessage: () -> String) {
	contract {
		returns() implies condition
		callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
	}

	ensureOrShift(condition, Failure.Kind.Invalid, lazyMessage)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun EffectScope<Failure>.ensureAuthenticated(condition: Boolean, lazyMessage: () -> String) {
	contract {
		returns() implies condition
		callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
	}

	ensureOrShift(condition, Failure.Kind.Unauthenticated, lazyMessage)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun EffectScope<Failure>.ensureAuthorized(condition: Boolean, lazyMessage: () -> String) {
	contract {
		returns() implies condition
		callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
	}

	ensureOrShift(condition, Failure.Kind.Unauthorized, lazyMessage)
}

@OptIn(ExperimentalContracts::class)
suspend inline fun EffectScope<Failure>.ensureFound(condition: Boolean, lazyMessage: () -> String) {
	contract {
		returns() implies condition
		callsInPlace(lazyMessage, InvocationKind.AT_MOST_ONCE)
	}

	ensureOrShift(condition, Failure.Kind.NotFound, lazyMessage)
}
