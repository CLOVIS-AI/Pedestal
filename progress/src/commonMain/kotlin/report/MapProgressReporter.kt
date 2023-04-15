package opensavvy.progress.report

import opensavvy.progress.Progress

private class MapProgressReporter(
    private val upstream: ProgressReporter,
    private val transform: (Progress) -> Progress,
) : ProgressReporter {

    override fun report(progress: Progress) {
        upstream.report(transform(progress))
    }

    override fun toString() = "Mapped($upstream)"
}

/**
 * Creates a new reporter that applies [transform] to each progress event it receives.
 */
fun ProgressReporter.map(transform: (Progress) -> Progress): ProgressReporter =
    MapProgressReporter(this, transform)
