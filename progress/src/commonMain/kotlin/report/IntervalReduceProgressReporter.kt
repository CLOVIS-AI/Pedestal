package opensavvy.progress.report

import opensavvy.progress.Progress
import opensavvy.progress.loading
import opensavvy.progress.report.IntervalReduceProgressReporter.Companion.defaultDone
import opensavvy.progress.report.IntervalReduceProgressReporter.Companion.defaultUnquantified

private class IntervalReduceProgressReporter(
    private val upstream: ProgressReporter,
    private val min: Double,
    private val max: Double,
    private val convertDone: Progress.Loading.Quantified,
    private val convertUnquantified: Progress.Loading.Quantified,
) : ProgressReporter {

    init {
        require(min < max) { "The minimum ($min) should be strictly inferior to the maximum ($max)" }
    }

    private val width = max - min

    override fun report(progress: Progress) {
        val received = when (progress) {
            is Progress.Loading.Quantified -> progress
            Progress.Loading.Unquantified -> convertUnquantified
            Progress.Done -> convertDone
        }

        upstream.report(loading(min + (received.normalized * width)))
    }

    override fun toString() = "$upstream.reduceToInterval($min..$max)"

    companion object {
        val defaultDone = loading(1.0)
        val defaultUnquantified = loading(0.5)
    }
}

/**
 * Creates a new reporter that proportionally confines progress events to the [min]..[max] range.
 *
 * @param treatDoneAs When a [Progress.Done] is received, it will be treated as if that value was received.
 * @param treatUnquantifiedAs When a [Progress.Loading.Unquantified] is received, it will be treated as if that value was received.
 */
fun ProgressReporter.reduceToInterval(
    min: Double,
    max: Double,
    treatDoneAs: Progress.Loading.Quantified = defaultDone,
    treatUnquantifiedAs: Progress.Loading.Quantified = defaultUnquantified,
): ProgressReporter =
    IntervalReduceProgressReporter(this, min, max, treatDoneAs, treatUnquantifiedAs)

/**
 * Creates a new reporter that proportionally confines progress events to [interval].
 *
 * @param treatDoneAs When a [Progress.Done] is received, it will be treated as if that value was received.
 * @param treatUnquantifiedAs When a [Progress.Loading.Unquantified] is received, it will be treated as if that value was received.
 */
fun ProgressReporter.reduceToInterval(
    interval: ClosedFloatingPointRange<Double>,
    treatDoneAs: Progress.Loading.Quantified = defaultDone,
    treatUnquantifiedAs: Progress.Loading.Quantified = defaultUnquantified,
) =
    reduceToInterval(interval.start, interval.endInclusive, treatDoneAs, treatUnquantifiedAs)
