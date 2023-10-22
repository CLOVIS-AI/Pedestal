package opensavvy.state.coroutines

import kotlinx.coroutines.flow.*
import opensavvy.progress.Progress
import opensavvy.progress.coroutines.report
import opensavvy.progress.done
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.asOutcome

/**
 * Successive values of the same object as time passes.
 *
 * Using this type alias instead of a regular flow communicates the intent that this flow contains successive values of
 * a unique target, and thus that each value emitted by the flow is more recent than any other previous values.
 *
 * To ignore loading values, see [filterNotLoading]. To suspend until the first value is available, use [now].
 */
typealias ProgressiveFlow<F, T> = Flow<ProgressiveOutcome<F, T>>

/**
 * Filters out all the values with a non-[done][done] [progress][ProgressiveOutcome.progress] from this flow.
 *
 * All [progress information][Progress] from filtered-out values is re-emitted to the calling flow.
 *
 * @see now Return only the first non-loading value, instead of returning a flow.
 */
fun <F, T> ProgressiveFlow<F, T>.filterNotLoading() = this
	.onEach { report(it.progress) }
	.filter { it.progress == done() }
	.mapNotNull { it.asOutcome() }

/**
 * Suspends until the first complete value is available (success or failure).
 *
 * This function assumes that the flow represents subsequent values of the same object as time passes.
 * In this interpretation, the first complete value is the current value; hence the name `now`.
 *
 * Because this function only returns a single value, it exits the reactive model. It is therefore discouraged to use
 * this function in UI code, as the calling code will not be made aware of new values arriving in the future.
 *
 * A complete value is a value which has a [progress][ProgressiveOutcome.progress] of [done].
 * All [progress information][Progress] from previous values is re-emitted in the calling coroutine.
 *
 * @throws NoSuchElementException In case the flow terminates before emitting a complete element.
 * @see filterNotLoading Return all complete elements instead of just the first one.
 */
suspend fun <F, T> ProgressiveFlow<F, T>.now() = this
	.filterNotLoading()
	.first()
