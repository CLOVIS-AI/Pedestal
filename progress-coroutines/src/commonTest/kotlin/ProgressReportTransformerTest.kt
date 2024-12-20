package opensavvy.progress.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.Progress
import opensavvy.progress.loading
import opensavvy.progress.report.ProgressReporter

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressReportTransformerTest : PreparedSpec({

    test("Using the progress transformer") {
        var value: Progress? = null

        withContext(ProgressReporter { value = it }.asCoroutineContext()) {
            report(loading(0.1))
            check(value == loading(0.1))

            mapProgressTo(0.2..0.5) {
                report(loading(0.0))
                check(value == loading(0.2))

                report(loading(0.5))
                check(value == loading(0.35))

                report(loading(1.0))
                check(value == loading(0.5))
            }

            report(loading(0.9))
            check(value == loading(0.9))
        }

        // Calling the function without reporter should no-op, NOT fail
        mapProgressTo(0.2..0.5) {
            report(loading(0.0))
            check(value == loading(0.9)) // The value shouldn't be impacted, since we should no-op
        }
    }

})
