package opensavvy.progress.report

import io.kotest.matchers.shouldBe
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.progress.loading

@Suppress("unused")
class MapProgressReporterTest : PreparedSpec({

    test("Should intercept reported values and transform them (replacing by done)") {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .map { done() }

        reporter.report(loading(0.5))
        value shouldBe done()
    }

    test("Should intercept reported values and transform them (increasing the value)") {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .map { it as Progress.Loading.Quantified; loading(it.normalized + 0.2) }

        reporter.report(loading(0.2))
        value shouldBe loading(0.4)
    }

    test("String representation") {
        val reporter = emptyProgressReporter()
            .reduceToInterval(0.1, 0.2)
            .map { it }

        check(reporter.toString() == "NoOpProgressReporter.reduceToInterval(0.1..0.2).map()")
    }
})
