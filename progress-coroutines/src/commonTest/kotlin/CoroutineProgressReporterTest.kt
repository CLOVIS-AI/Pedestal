package opensavvy.progress.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import opensavvy.progress.Progress
import opensavvy.progress.loading
import opensavvy.progress.report.ProgressReporter
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineProgressReporterTest {

    @Test
    fun report() = runTest {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .asCoroutineContext()

        withContext(reporter) {
            report(loading(0.2))
        }

        assertEquals(loading(0.2), value)
    }

    @Test
    fun reportWithoutReporter() = runTest {
        report(loading(0.2)) // does nothing, doesn't crash
    }

    @Test
    fun withReporter() = runTest {
        var value: Progress? = null

        reportProgress({ value = it }) {
            report(loading(0.2))
        }

        assertEquals(loading(0.2), value)
    }
}
