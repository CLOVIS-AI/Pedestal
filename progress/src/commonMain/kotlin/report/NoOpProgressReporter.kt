package opensavvy.progress.report

import opensavvy.progress.Progress

// Private object because otherwise IDEA wants to statically import NoOpProgressReporter.report everywhere, which
// is a foot-gun.
private data object NoOpProgressReporter : ProgressReporter {
    override fun report(progress: Progress) {}
}

/**
 * A [ProgressReporter] implementation that does nothing.
 */
fun emptyProgressReporter(): ProgressReporter = NoOpProgressReporter
