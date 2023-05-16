package opensavvy.progress.report

import opensavvy.progress.loading
import kotlin.test.Test

class NoOpProgressReporterTest {

	@Test
	fun coverage() {
		// it does nothing anyway…
		emptyProgressReporter().report(loading(0.5))
	}
}
