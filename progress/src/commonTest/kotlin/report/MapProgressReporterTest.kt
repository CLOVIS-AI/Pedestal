package opensavvy.progress.report

import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.progress.loading
import kotlin.test.Test
import kotlin.test.assertEquals

class MapProgressReporterTest {

    @Test
    fun replaceByDone() {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .map { done() }

        reporter.report(loading(0.5))
        assertEquals(done(), value)
    }

    @Test
    fun addOne() {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .map { it as Progress.Loading.Quantified; loading(it.normalized + 0.2) }

        reporter.report(loading(0.2))
        assertEquals(loading(0.4), value)
    }

    @Test
    fun string() {
        val reporter = emptyProgressReporter()
            .reduceToInterval(0.1, 0.2)
            .map { it }

        assertEquals("NoOpProgressReporter.reduceToInterval(0.1..0.2).map()", reporter.toString())
    }
}
