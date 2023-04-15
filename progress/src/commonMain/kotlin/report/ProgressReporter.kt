package opensavvy.progress.report

import opensavvy.progress.Progress

/**
 * SAM interface to communicate progress to a caller.
 *
 * Instances of this interface can be created by a caller and passed to a downstream user.
 * The downstream user can then call the [report] function to communicate its progress to the caller.
 */
fun interface ProgressReporter {

    /**
     * Reports that the current task has reached [progress].
     */
    fun report(progress: Progress)

    companion object

    /**
     * A [ProgressReporter] implementation that does nothing.
     */
    object NoOp : ProgressReporter {
        override fun report(progress: Progress) {}
    }
}
