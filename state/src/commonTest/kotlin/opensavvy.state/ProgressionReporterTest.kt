package opensavvy.state

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import opensavvy.state.Progression.Companion.done
import opensavvy.state.Progression.Companion.loading
import opensavvy.state.ProgressionReporter.Companion.progressionReporter
import opensavvy.state.ProgressionReporter.Companion.report
import opensavvy.state.ProgressionReporter.Companion.transformQuantifiedProgress
import kotlin.test.Test
import kotlin.test.assertEquals

class ProgressionReporterTest {

	@Test
	fun simple() = runTest {
		val reporter = progressionReporter()

		launch {
			withContext(reporter) {
				report(loading(0.0))
				delay(100)
				report(loading(0.5))
				delay(100)
				report(loading(1.0))
			}
		}

		yield()

		assertEquals(loading(0.0), reporter.progress.value)
		delay(150)
		assertEquals(loading(0.5), reporter.progress.value)
		delay(150)
		assertEquals(loading(1.0), reporter.progress.value)
	}

	@Test
	fun nested() = runTest {
		val reporter = progressionReporter()

		launch {
			withContext(reporter) {
				transformQuantifiedProgress({ loading(it.normalized / 2) }) {
					report(loading())
					delay(10)
					report(done())
					delay(10)
					report(loading(0.5))
				}
				delay(100)
				report(loading(0.5))
			}
		}

		yield()

		delay(50)
		assertEquals(loading(0.25), reporter.progress.value)
		delay(150)
		assertEquals(loading(0.5), reporter.progress.value)
	}

	@Test
	fun noReporter() = runTest {
		// There is no ProgressionReporter in this coroutine, so reporting progress should do nothing
		report(loading())
		report(loading(0.5))
		report(done())
	}

}
