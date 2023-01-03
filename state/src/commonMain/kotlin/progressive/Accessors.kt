package opensavvy.state.progressive

import arrow.core.left
import arrow.core.right
import kotlinx.coroutines.flow.*
import opensavvy.state.Progression
import opensavvy.state.ProgressionReporter.Companion.report
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome.*
import opensavvy.state.progressive.ProgressiveOutcome.Companion.component1
import opensavvy.state.progressive.ProgressiveOutcome.Companion.component2

//region Regular

/**
 * Returns [Success.value], or `null` if this outcome is not successful.
 */
val <T : Any> ProgressiveOutcome<T>.valueOrNull: T?
	get() = (this as? Success<T>)?.value

/**
 * Returns [Failure.failure], or `null` if this outcome is not a failure.
 */
val ProgressiveOutcome<*>.failureOrNull: opensavvy.state.Failure?
	get() = (this as? Failure)?.failure

/**
 * Converts this progressive outcome into a [regular outcome][Outcome].
 *
 * Because regular outcomes do not have a concept of progression, the progress information is lost.
 * To access both the outcome and the progression information, consider using destructuration instead:
 * ```kotlin
 * val (outcome, progression) = /* ProgressiveOutcome */
 * ```
 */
fun <T> ProgressiveOutcome<T>.asOutcome(): Outcome<T>? = when (this) {
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
fun <T> Flow<ProgressiveOutcome<T>>.filterDone() = onEach { report(it.progress) }
	.filter { it.progress == Progression.done() }
	.mapNotNull { it.asOutcome() }

/**
 * Suspends until the first finished value is available (success or failure).
 *
 * All progress information is re-emitted in the calling coroutine.
 *
 * @throws NoSuchElementException if the flow has no finished elements.
 */
suspend fun <T> Flow<ProgressiveOutcome<T>>.firstValue() = filterDone()
	.first()

/**
 * Splits this progressive outcome into its outcome and progress information.
 */
fun <T> Flow<ProgressiveOutcome<T>>.asOutcomeAndProgress(): Flow<Pair<Outcome<T>?, Progression>> =
	map { (out, progress) -> out to progress }

//endregion
