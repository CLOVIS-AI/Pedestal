package opensavvy.progress.report

import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.progress.loading
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class IntervalReduceProgressReporterTest {

    @Test
    fun cannotCreateInvalidRange() {
        assertFails {
            ProgressReporter { }
                .reduceToInterval(0.7, 0.2)
        }
    }

    @Test
    fun reduceInterval() {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2..0.4)

        reporter.report(loading(0.1))
        assertEquals(loading(0.22), value)
    }

    @Test
    fun reduceMinMax() {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2, 0.4)

        reporter.report(loading(0.1))
        assertEquals(loading(0.22), value)
    }

    @Test
    fun reduceDone() {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2..0.4)

        reporter.report(done())
        assertEquals(loading(0.4), value)
    }

    @Test
    fun reduceUnquantified() {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2..0.4)

        reporter.report(loading())
        assertEquals(loading(0.3), value)
    }

    @Test
    fun reduceZero() {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2..0.4)

        reporter.report(loading(0.0))
        assertEquals(loading(0.2), value)
    }

    @Test
    fun reduceOne() {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2..0.4)

        reporter.report(loading(1.0))
        assertEquals(loading(0.4), value)
    }

}
