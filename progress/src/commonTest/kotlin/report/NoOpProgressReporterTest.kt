package opensavvy.progress.report

import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading

@Suppress("unused")
class NoOpProgressReporterTest : PreparedSpec({
	test("No-op") {
		// it does nothing anyway…
		emptyProgressReporter().report(loading(0.5))
	}
})
