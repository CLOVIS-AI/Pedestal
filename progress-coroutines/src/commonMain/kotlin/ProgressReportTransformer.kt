package opensavvy.progress.coroutines

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.withContext
import opensavvy.progress.loading
import opensavvy.progress.report.ProgressReporter
import opensavvy.progress.report.emptyProgressReporter
import opensavvy.progress.report.reduceToInterval

/**
 * Passes the current [ProgressReporter] to [createChildReporter] to create a new child reporter of the current coroutine,
 * and adds it to [block]'s context.
 */
suspend fun <R> transformProgress(
    createChildReporter: (ProgressReporter) -> ProgressReporter,
    block: suspend () -> R
): R {
    val upstreamReporter = currentCoroutineContext()[CoroutineProgressReporter.Key]
        ?: emptyProgressReporter()

    val reporter = createChildReporter(upstreamReporter)
        .asCoroutineContext()

    return withContext(reporter) {
        block()
    }
}

/**
 * Reduces progress events emitted in [block] to fit into [progressInterval].
 *
 * @see reduceToInterval
 * @see transformProgress
 */
suspend fun <R> mapProgressTo(progressInterval: ClosedFloatingPointRange<Double>, block: suspend () -> R): R =
    transformProgress({ it.reduceToInterval(progressInterval) }) {
        try {
            report(loading(0.0))
            block()
        } finally {
            report(loading(1.0))
        }
    }
