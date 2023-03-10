package opensavvy.state.progressive

import arrow.core.continuations.EffectScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import opensavvy.state.Failure
import opensavvy.state.Progression
import opensavvy.state.ProgressionReporter
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.out

/**
 * Adds [progress] information to this outcome to make it a [ProgressiveOutcome].
 *
 * Because regular outcomes cannot be unfinished, this function never returns [ProgressiveOutcome.Empty].
 */
fun <T> Outcome<T>.withProgress(progress: Progression = Progression.done()) = fold(
	ifLeft = { ProgressiveOutcome.Failure(it, progress) },
	ifRight = { ProgressiveOutcome.Success(it, progress) },
)

/**
 * Replaces the [progress] information from this progressive outcome.
 */
fun <T> ProgressiveOutcome<T>.copy(progress: Progression.Loading) = when (this) {
	is ProgressiveOutcome.Empty -> ProgressiveOutcome.Empty(progress)
	is ProgressiveOutcome.Failure -> ProgressiveOutcome.Failure(failure, progress)
	is ProgressiveOutcome.Success -> ProgressiveOutcome.Success(value, progress)
}

/**
 * Performs some calculation which may fail, capturing all progression events in the process.
 *
 * For performance reasons, the [ProgressionReporter.report] function is shadowed by [ProgressiveOutcomeCollector.report], which
 * bypasses [ProgressionReporter] and directly pushes the event into the resulting flow.
 * Calls to [ProgressionReporter.report] are not affected by this function (they pass through to the closest
 * parent [ProgressionReporter]). If you wish to capture them, use [captureProgress].
 */
fun <T> progressive(block: suspend ProgressiveOutcomeCollector<T>.() -> T): Flow<ProgressiveOutcome<T>> = flow {
	emit(
		out {
			block(ProgressiveOutcomeCollector(this@flow, this@out))
		}.withProgress()
	)
}

class ProgressiveOutcomeCollector<T>(
	private val flowCollector: FlowCollector<ProgressiveOutcome<T>>,
	private val effectScope: EffectScope<Failure>,
) : EffectScope<Failure> by effectScope {

	suspend fun report(progress: Progression.Loading) {
		flowCollector.emit(ProgressiveOutcome.Empty(progress))
	}
}
