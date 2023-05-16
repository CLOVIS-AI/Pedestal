package opensavvy.progress.coroutines

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import opensavvy.progress.Progress
import opensavvy.progress.loading
import opensavvy.progress.report.ProgressReporter

/**
 * [ProgressReporter] implementation which stores the latest progress information in [progress], a [StateFlow].
 */
class StateFlowProgressReporter : ProgressReporter {
    private val state = MutableStateFlow<Progress>(loading(0.0))

    val progress = state as StateFlow<Progress>

    override fun report(progress: Progress) {
        state.value = progress
    }

    override fun toString() = "StateFlowProgressReporter(progress=${progress.value})"
}
