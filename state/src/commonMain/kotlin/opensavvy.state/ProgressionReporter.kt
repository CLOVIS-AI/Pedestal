package opensavvy.state

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import opensavvy.state.Progression.Companion.loading
import opensavvy.state.ProgressionReporter.Companion.progressionReporter
import opensavvy.state.ProgressionReporter.Companion.report
import opensavvy.state.ProgressionReporter.Companion.transformProgress
import opensavvy.state.ProgressionReporter.Companion.transformQuantifiedProgress
import opensavvy.state.ProgressionReporter.StateFlowReporter
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * Stores progression information about the currently running task.
 *
 * Coroutines pass a [CoroutineContext] object around which stores information about the currently-running process.
 * To report the current [Progression] of a coroutine, [ProgressionReporter] can be added to the context of a coroutine.
 * It is then possible to access the current progression using [progress][StateFlowReporter.progress].
 * ```kotlin
 * val scope = CoroutineScope()
 * val reporter = progressionReporter()
 *
 * scope.launch {
 *     withContext(reporter) {
 *         repeat(100) {
 *             delay(10)
 *             report(loading(it / 100.0))
 *         }
 *     }
 * }
 *
 * reporter.progress.collect {
 *     println("Current progress: $it")
 * }
 * ```
 *
 * To instantiate a progression reporter, see [progressionReporter].
 * To report the current loading, see [report].
 * To manage subtasks, see [transformProgress] and [transformQuantifiedProgress].
 */
abstract class ProgressionReporter : AbstractCoroutineContextElement(Key) {

	/**
	 * Notifies this reporter of the current [progression].
	 */
	abstract suspend fun emit(progression: Progression)

	/**
	 * Implementation of [ProgressionReporter] using a [StateFlow].
	 *
	 * The internal StateFlow is exposed as [progress].
	 */
	class StateFlowReporter : ProgressionReporter() {
		private val state = MutableStateFlow<Progression>(loading())

		/**
		 * The current reported progress.
		 */
		val progress = state as StateFlow<Progression>

		override suspend fun emit(progression: Progression) {
			state.value = progression
		}

		override fun toString() = state.value.toString()
	}

	private class ChildReporter(
		private val parent: ProgressionReporter,
		private val transform: (Progression) -> Progression,
	) : ProgressionReporter() {
		override suspend fun emit(progression: Progression) {
			parent.emit(transform(progression))
		}
	}

	object Key : CoroutineContext.Key<ProgressionReporter>

	companion object {

		//region Builders

		/**
		 * Creates a default [ProgressionReporter].
		 */
		fun progressionReporter() = StateFlowReporter()

		/**
		 * Declares [block] as a subtask of the current coroutine.
		 *
		 * Inside [block], all progression events are passed to [transform] before being transmitted to the parent coroutine.
		 */
		@OptIn(ExperimentalContracts::class)
		suspend fun transformProgress(
			transform: (Progression) -> Progression,
			block: suspend CoroutineScope.() -> Unit,
		) {
			contract {
				callsInPlace(block, InvocationKind.AT_MOST_ONCE)
			}

			val reporter = currentCoroutineContext()[Key]

			if (reporter != null)
				withContext(ChildReporter(reporter, transform), block)
		}

		/**
		 * Declares [block] as a subtask of the current coroutine.
		 *
		 * Inside [block], loading events are processed by this function.
		 * - if the coroutine reports a [quantified loading][Progression.Loading.Quantified], it is passed through
		 * [transform] and emitted to the parent coroutine,
		 * - if the coroutine reports an [unquantified loading][Progression.Loading.Unquantified], it is passed through
		 * [transform] as a quantified loading of 50% and emitted to the parent coroutine,
		 * - if the coroutine reports a [done][Progression.Done], it is passed through unchanged to the parent.
		 */
		@OptIn(ExperimentalContracts::class)
		suspend fun transformQuantifiedProgress(
			transform: (Progression.Loading.Quantified) -> Progression,
			block: suspend CoroutineScope.() -> Unit,
		) {
			contract {
				callsInPlace(block, InvocationKind.AT_MOST_ONCE)
			}

			val reporter = currentCoroutineContext()[Key]

			if (reporter != null)
				withContext(
					ChildReporter(reporter) {
						when (it) {
							is Progression.Loading.Quantified -> transform(it)
							is Progression.Loading.Unquantified -> transform(loading(0.5))
							Progression.Done -> it
						}
					},
					block
				)
		}

		//endregion

		/**
		 * Report [progression] to the [ProgressionReporter] in the current coroutine context.
		 *
		 * If there is currently no [ProgressionReporter], this function does nothing.
		 */
		@Suppress("MemberVisibilityCanBePrivate")
		suspend fun report(progression: Progression) {
			val reporter = currentCoroutineContext()[Key] ?: return
			reporter.emit(progression)
		}
	}
}
