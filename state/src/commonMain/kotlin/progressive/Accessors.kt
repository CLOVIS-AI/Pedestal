package opensavvy.state.progressive

import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome.*

// region Get or null

/**
 * Returns [Success.value], or `null` if this outcome is not successful.
 */
val <T : Any> ProgressiveOutcome<*, T>.valueOrNull: T?
	get() = (this as? Success)?.value

/**
 * Returns [Failure.failure], or `null` if this outcome is not a failure.
 */
val <F> ProgressiveOutcome<F, *>.failureOrNull: F?
	get() = (this as? Failure)?.failure

// endregion
// region Safe get via Nothing

/**
 * Returns [Success.value].
 */
val <T> ProgressiveOutcome<Nothing, T>.value: T
	// This cast is safe, because a Success of Nothing is impossible
	get() = (this as Success).value

/**
 * Returns [Failure.failure].
 */
val <F> ProgressiveOutcome<F, Nothing>.failure: F
	// This cast is safe, because a Success of Nothing is impossible
	get() = (this as Failure).failure

// endregion
// region Conversion

/**
 * Converts this progressive outcome into a [regular outcome][Outcome].
 *
 * Because regular outcomes do not have a concept of progression, the progress information is lost.
 * To access both the outcome and the progression information, consider using destructuration instead:
 * ```kotlin
 * val (outcome, progression) = /* ProgressiveOutcome */
 * ```
 */
fun <F, T> ProgressiveOutcome<F, T>.asOutcome() = when (this) {
	is Success -> Outcome.Success(value)
	is Failure -> Outcome.Failure(failure)
	is Incomplete -> null
}

//endregion
//region Destructuration

/**
 * Syntax sugar for [asOutcome].
 */
operator fun <F, T> ProgressiveOutcome<F, T>.component1() = asOutcome()

/**
 * Syntax sugar for [ProgressiveOutcome.progress].
 */
operator fun ProgressiveOutcome<*, *>.component2() = progress

//endregion
