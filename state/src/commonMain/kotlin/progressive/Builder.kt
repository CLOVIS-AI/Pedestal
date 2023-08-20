package opensavvy.state.progressive

import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.state.outcome.Outcome

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
fun <Value> Value.success(progress: Progress = done()) = ProgressiveOutcome.Success(this, progress)

/**
 * Convenience function to instantiate a [ProgressiveOutcome.Failure].
 */
fun <Failure> Failure.failed(progress: Progress = done()) = ProgressiveOutcome.Failure(this, progress)
