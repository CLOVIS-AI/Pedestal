package opensavvy.state.progressive

import opensavvy.state.Progression
import opensavvy.state.progressive.ProgressiveSlice.*
import opensavvy.state.slice.Slice

/**
 * A [Slice] with integrated [Progression] management.
 *
 * There are three possible cases:
 * - [Empty] if the task has started but no value is currently available,
 * - [Success] if a successful result is available (see [Success.value]),
 * - [Failure] if a failed result is available (see [Failure.failure]).
 *
 * Note that in cases, a progressive slice may be currently loading.
 * - [Empty] must be loading,
 * - [Success] may be loading if the task is currently querying a newer value than the one it stores,
 * - [Failure] may be loading if the task is currently retrying the operation.
 *
 * To create a progressive slice from a function returning a regular slice, use [captureProgress].
 *
 * To access the inner slice and progression, you can use [asSlices] or the destructuring operator:
 * ```kotlin
 * val (slice, progression) = /* ProgressiveSlice */
 * ```
 *
 * To create progressive slices from computations, use the [progressiveSlice] or [captureProgress].
 */
sealed class ProgressiveSlice<out T> {

	/**
	 * The current progression of this slice.
	 *
	 * For more information, see [ProgressiveSlice].
	 */
	abstract val progress: Progression

	/**
	 * The operation is ongoing, but we do not know if it will be successful or a failure.
	 */
	data class Empty(
		override val progress: Progression.Loading = Progression.loading(),
	) : ProgressiveSlice<Nothing>()

	/**
	 * The latest known result of the operation was a success, available as [value].
	 *
	 * If [progress] is loading, this means the operation has been retried in an attempt to access a more up-to-date
	 * version.
	 */
	data class Success<T>(
		val value: T,
		override val progress: Progression = Progression.done(),
	) : ProgressiveSlice<T>()

	/**
	 * The latest known result of the operation was a failure, available as [failure].
	 *
	 * If [progress] is loading, this means the operation has been retried in an attempt to access a more up-to-date
	 * version.
	 */
	data class Failure(
		val failure: opensavvy.state.Failure,
		override val progress: Progression = Progression.done(),
	) : ProgressiveSlice<Nothing>()

	companion object {

		operator fun <T> ProgressiveSlice<T>.component1() = asSlice()
		operator fun <T> ProgressiveSlice<T>.component2() = progress

	}

}
