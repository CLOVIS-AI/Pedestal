package opensavvy.progress.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.Progress
import opensavvy.progress.loading
import opensavvy.progress.report.ProgressReporter

@OptIn(ExperimentalCoroutinesApi::class)
class CoroutineProgressReporterTest : PreparedSpec({

    test("Report a value through the coroutine context") {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .asCoroutineContext()

        withContext(reporter) {
            report(loading(0.2))
        }

        check(value == loading(0.2))
    }

    test("Report without a reporter should do nothing") {
        report(loading(0.2)) // does nothing, doesn't crash
    }

    test("Report a value using a callback") {
        var value: Progress? = null

        reportProgress({ value = it }) {
            report(loading(0.2))
        }

        check(value == loading(0.2))
    }

})
