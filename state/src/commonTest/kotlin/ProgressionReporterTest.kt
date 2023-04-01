package opensavvy.state

import kotlinx.coroutines.*
import kotlinx.coroutines.test.runTest
import opensavvy.progress.coroutines.StateFlowProgressReporter
import opensavvy.progress.coroutines.asCoroutineContext
import opensavvy.progress.coroutines.mapProgressTo
import opensavvy.progress.coroutines.report
import opensavvy.progress.done
import opensavvy.progress.loading
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressionReporterTest {

	@Test
	fun simple() = runTest {
		val reporter = StateFlowProgressReporter()

		launch {
			withContext(reporter.asCoroutineContext()) {
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
		val reporter = StateFlowProgressReporter()

		launch {
			withContext(reporter.asCoroutineContext()) {
				mapProgressTo(0.0..0.5) {
					report(loading())
					delay(10)
					report(done())
					delay(10)
					report(loading(0.5))
					delay(100)
				}
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
