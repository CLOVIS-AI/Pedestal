package opensavvy.progress.report

import opensavvy.progress.Progress

// Private object because otherwise IDEA wants to statically import NoOpProgressReporter.report everywhere, which
// is a foot-gun.
private object NoOpProgressReporter : ProgressReporter {
    override fun report(progress: Progress) {}

    override fun toString() = "NoOpProgressReporter"
}

/**
 * A [ProgressReporter] implementation that does nothing.
 */
fun emptyProgressReporter(): ProgressReporter = NoOpProgressReporter
