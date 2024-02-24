package opensavvy.progress.report

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.Progress
import opensavvy.progress.done
import opensavvy.progress.loading

@Suppress("unused")
class IntervalReduceProgressReporterTest : PreparedSpec({
    test("Cannot create an invalid range") {
        shouldThrow<IllegalArgumentException> {
            emptyProgressReporter().reduceToInterval(0.7, 0.2)
        }
    }

    test("Reduce with the range syntax") {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2..0.4)

        reporter.report(loading(0.1))
        value shouldBe loading(0.22)
    }

    test("Reduce with the min-max syntax") {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2, 0.4)

        reporter.report(loading(0.1))
        value shouldBe loading(0.22)
    }

    test("Reducing the 'done' event should return the range maximum") {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2..0.4)

        reporter.report(done())
        value shouldBe loading(0.4)
    }

    test("Reducing an unquantified loading event should return the range middle") {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2..0.4)

        reporter.report(loading())
        value shouldBe loading(0.3)
    }

    test("Reducing 0 should give the range minimum") {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2..0.4)

        reporter.report(loading(0.0))
        value shouldBe loading(0.2)
    }

    test("Reducing 1 should give the range maximum") {
        var value: Progress? = null

        val reporter = ProgressReporter { value = it }
            .reduceToInterval(0.2..0.4)

        reporter.report(loading(1.0))
        value shouldBe loading(0.4)
    }
})
