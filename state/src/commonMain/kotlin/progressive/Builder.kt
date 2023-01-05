package opensavvy.state.progressive

import arrow.core.continuations.EffectScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import opensavvy.state.Failure
import opensavvy.state.Progression
import opensavvy.state.ProgressionReporter
import opensavvy.state.slice.Slice
import opensavvy.state.slice.slice

/**
 * Adds [progress] information to this slice to make it a [ProgressiveSlice].
 *
 * Because regular slices cannot be unfinished, this function never returns [ProgressiveSlice.Empty].
 */
fun <T> Slice<T>.withProgress(progress: Progression = Progression.done()) = fold(
	ifLeft = { ProgressiveSlice.Failure(it, progress) },
	ifRight = { ProgressiveSlice.Success(it, progress) },
)

/**
 * Replaces the [progress] information from this progressive slice.
 */
fun <T> ProgressiveSlice<T>.copy(progress: Progression.Loading) = when (this) {
	is ProgressiveSlice.Empty -> ProgressiveSlice.Empty(progress)
	is ProgressiveSlice.Failure -> ProgressiveSlice.Failure(failure, progress)
	is ProgressiveSlice.Success -> ProgressiveSlice.Success(value, progress)
}

/**
 * Performs some calculation which may fail, capturing all progression events in the process.
 *
 * For performance reasons, the [ProgressionReporter.report] function is shadowed by [ProgressiveSliceCollector.report], which
 * bypasses [ProgressionReporter] and directly pushes the event into the resulting flow.
 * Calls to [ProgressionReporter.report] are not affected by this function (they pass through to the closest
 * parent [ProgressionReporter]). If you wish to capture them, use [captureProgress].
 */
fun <T> progressiveSlice(block: suspend ProgressiveSliceCollector<T>.() -> T): Flow<ProgressiveSlice<T>> = flow {
	emit(
		slice {
			block(ProgressiveSliceCollector(this@flow, this@slice))
		}.withProgress()
	)
}

class ProgressiveSliceCollector<T>(
	private val flowCollector: FlowCollector<ProgressiveSlice<T>>,
	private val effectScope: EffectScope<Failure>,
) : EffectScope<Failure> by effectScope {

	suspend fun report(progress: Progression.Loading) {
		flowCollector.emit(ProgressiveSlice.Empty(progress))
	}
}
