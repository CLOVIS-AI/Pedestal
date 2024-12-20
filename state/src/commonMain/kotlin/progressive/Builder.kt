package opensavvy.state.progressive

import opensavvy.progress.ExperimentalProgressApi
import opensavvy.progress.Progress
import opensavvy.progress.Progressive
import opensavvy.progress.done
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome.Unsuccessful

/**
 * Adds [progress] information to this outcome to make it a [ProgressiveOutcome].
 *
 * Because regular outcomes cannot be unfinished, this function never returns [ProgressiveOutcome.Incomplete].
 */
fun <Failure, Value> Outcome<Failure, Value>.withProgress(progress: Progress = done()) = when (this) {
	is Outcome.Success -> ProgressiveOutcome.Success(value, progress)
	is Outcome.Failure -> ProgressiveOutcome.Failure(failure, progress)
}

/**
 * Replaces the [progress] information from this progressive outcome.
 */
fun <Failure, Value> ProgressiveOutcome<Failure, Value>.copy(progress: Progress.Loading) = when (this) {
	is ProgressiveOutcome.Incomplete -> ProgressiveOutcome.Incomplete(progress)
	is ProgressiveOutcome.Failure -> ProgressiveOutcome.Failure(failure, progress)
	is ProgressiveOutcome.Success -> ProgressiveOutcome.Success(value, progress)
}

/**
 * Convenience function to instantiate a [ProgressiveOutcome.Success].
 */
fun <Value> Value.successfulWithProgress(progress: Progress = done()) = ProgressiveOutcome.Success(this, progress)

/**
 * Convenience function to instantiate a [ProgressiveOutcome.Failure].
 */
fun <Failure> Failure.failedWithProgress(progress: Progress = done()) = ProgressiveOutcome.Failure(this, progress)

/**
 * Type-safe way to convert between an [Unsuccessful] and a [ProgressiveOutcome].
 *
 * In theory, this would be an implicit upcast. However, [ProgressiveOutcome] is a `class`,
 * and [Unsuccessful] is an `interface`, so the subtype relationship cannot be declared to the compiler.
 */
@ExperimentalProgressiveRaiseApi
fun <Failure> Unsuccessful<Failure>.upcast(): ProgressiveOutcome<Failure, Nothing> =
	when (this) {
		is ProgressiveOutcome.Failure -> this
		is ProgressiveOutcome.Incomplete -> this
	}

// region Conversion with Progress' Progressive

/**
 * Converts an [Outcome] nested in a [Progressive] into a [ProgressiveOutcome].
 *
 * @see explode Reverse operation.
 */
@ExperimentalProgressApi
fun <Failure, Value> Progressive<Outcome<Failure, Value>>.flatten(): ProgressiveOutcome<Failure, Value> =
	value.withProgress(progress)

/**
 * Converts a [ProgressiveOutcome] into a [Progressive] [Outcome].
 *
 * In the case where in the input [ProgressiveOutcome] is [Incomplete][ProgressiveOutcome.Incomplete], `null` is returned.
 *
 * @see flatten Reverse operation.
 */
@ExperimentalProgressApi
fun <Failure, Value> ProgressiveOutcome<Failure, Value>.explode(): Progressive<Outcome<Failure, Value>?> =
	Progressive(asOutcome(), progress)

// endregion
