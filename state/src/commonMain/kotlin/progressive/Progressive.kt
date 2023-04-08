package opensavvy.state.progressive

import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.progress.loading
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome.*

/**
 * A [Outcome] with integrated [Progress] management.
 *
 * There are three possible cases:
 * - [Empty] if the task has started but no value is currently available,
 * - [Success] if a successful result is available (see [Success.value]),
 * - [Failure] if a failed result is available (see [Failure.failure]).
 *
 * Note that in cases, a progressive outcome may be currently loading.
 * - [Empty] must be loading,
 * - [Success] may be loading if the task is currently querying a newer value than the one it stores,
 * - [Failure] may be loading if the task is currently retrying the operation.
 *
 * To create a progressive outcome from a function returning a regular outcome, use [captureProgress].
 *
 * To access the inner outcome and progression, you can use [asOutcomeAndProgress] or the destructuring operator:
 * ```kotlin
 * val (out, progression) = /* ProgressiveOutcome */
 * ```
 *
 * To create progressive outcomes from computations, use the [progressive] or [captureProgress] builders.
 */
sealed class ProgressiveOutcome<out T> {

	/**
	 * The current progression of this outcome.
	 *
	 * For more information, see [ProgressiveOutcome].
	 */
	abstract val progress: Progress

	/**
	 * The operation is ongoing, but we do not know if it will be successful or a failure.
	 */
	data class Empty(
		override val progress: Progress.Loading = loading(),
	) : ProgressiveOutcome<Nothing>()

	/**
	 * The latest known result of the operation was a success, available as [value].
	 *
	 * If [progress] is loading, this means the operation has been retried in an attempt to access a more up-to-date
	 * version.
	 */
	data class Success<T>(
		val value: T,
		override val progress: Progress = done(),
	) : ProgressiveOutcome<T>()

	/**
	 * The latest known result of the operation was a failure, available as [failure].
	 *
	 * If [progress] is loading, this means the operation has been retried in an attempt to access a more up-to-date
	 * version.
	 */
	data class Failure(
		val failure: opensavvy.state.Failure,
		override val progress: Progress = done(),
	) : ProgressiveOutcome<Nothing>()

	companion object {

		operator fun <T> ProgressiveOutcome<T>.component1() = asOutcome()
		operator fun <T> ProgressiveOutcome<T>.component2() = progress

	}

}
