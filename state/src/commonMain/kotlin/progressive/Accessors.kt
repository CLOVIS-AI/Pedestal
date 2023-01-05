package opensavvy.state.progressive

import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.flow.*
import opensavvy.state.Progression
import opensavvy.state.ProgressionReporter.Companion.report
import opensavvy.state.progressive.ProgressiveSlice.*
import opensavvy.state.progressive.ProgressiveSlice.Companion.component1
import opensavvy.state.progressive.ProgressiveSlice.Companion.component2
import opensavvy.state.slice.Slice

//region Regular

/**
 * Returns [Success.value], or `null` if this slice is not successful.
 */
val <T : Any> ProgressiveSlice<T>.valueOrNull: T?
	get() = (this as? Success<T>)?.value

/**
 * Returns [Failure.failure], or `null` if this slice is not a failure.
 */
val ProgressiveSlice<*>.failureOrNull: opensavvy.state.Failure?
	get() = (this as? Failure)?.failure

/**
 * Converts this progressive slice into a [regular slice][Slice].
 *
 * Because regular slices do not have a concept of progression, the progress information is lost.
 * To access both the slice and the progression information, consider using destructuration instead:
 * ```kotlin
 * val (slice, progression) = /* ProgressiveSlice */
 * ```
 */
fun <T> ProgressiveSlice<T>.asSlice(): Slice<T>? = when (this) {
	is Empty -> null
	is Success -> value.right()
	is Failure -> failure.left()
}

//endregion
//region Flow

/**
 * Filters out all the [Empty] and loading values from this flow.
 *
 * All progress information is re-emitted in the calling flow.
 */
fun <T> Flow<ProgressiveSlice<T>>.filterDone() = onEach { report(it.progress) }
	.filter { it.progress == Progression.done() }
	.mapNotNull { it.asSlice() }

/**
 * Suspends until the first finished value is available (success or failure).
 *
 * All progress information is re-emitted in the calling coroutine.
 *
 * @throws NoSuchElementException if the flow has no finished elements.
 */
suspend fun <T> Flow<ProgressiveSlice<T>>.firstValue() = filterDone()
	.first()

/**
 * Splits this progressive slice into its slice and progress information.
 */
fun <T> Flow<ProgressiveSlice<T>>.asSlices(): Flow<Pair<Slice<T>?, Progression>> =
	map { (slice, progress) -> slice to progress }

//endregion
