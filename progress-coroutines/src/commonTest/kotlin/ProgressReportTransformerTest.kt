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
class ProgressReportTransformerTest {

    @Test
    fun test() = runTest {
        var value: Progress? = null

        withContext(ProgressReporter { value = it }.asCoroutineContext()) {
            report(loading(0.1))
            assertEquals(loading(0.1), value)

            mapProgressTo(0.2..0.5) {
                report(loading(0.0))
                assertEquals(loading(0.2), value)

                report(loading(0.5))
                assertEquals(loading(0.35), value)

                report(loading(1.0))
                assertEquals(loading(0.5), value)
            }

            report(loading(0.9))
            assertEquals(loading(0.9), value)
        }

        // Calling the function without reporter should no-op, NOT fail
        mapProgressTo(0.2..0.5) {
            report(loading(0.0))
            assertEquals(loading(0.9), value) // The value shouldn't be impacted, since we should no-op
        }
    }
}
