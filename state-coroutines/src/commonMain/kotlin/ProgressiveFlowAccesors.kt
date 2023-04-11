package opensavvy.state.coroutines

import kotlinx.coroutines.flow.*
import opensavvy.progress.Progress
import opensavvy.progress.coroutines.report
import opensavvy.progress.done
import opensavvy.state.failure.Failure
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.asOutcome

/**
 * Filters out all the [ProgressiveOutcome.Incomplete] values from this flow.
 *
 * All [progress information][Progress] is re-emitted to the calling flow.
 */
fun <F : Failure, T> Flow<ProgressiveOutcome<F, T>>.filterComplete() = this
    .onEach { report(it.progress) }
    .filter { it.progress == done() }
    .mapNotNull { it.asOutcome() }

/**
 * Suspends until the first complete value is available (success or failure).
 *
 * All [progress information][Progress] is re-emitted in the calling coroutine.
 *
 * @throws NoSuchElementException if the flow has no complete elements.
 */
suspend fun <F : Failure, T> Flow<ProgressiveOutcome<F, T>>.firstValue() = this
    .filterComplete()
    .first()
