package opensavvy.progress.coroutines

import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.yield
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.launch
import opensavvy.progress.done
import opensavvy.progress.loading

class StateFlowProgressReporterTest : PreparedSpec({

	test("Reporting progress events through a StateFlow") {
		val reporter = StateFlowProgressReporter()

		launch {
			reporter.report(loading(0.2))
			yield()

			reporter.report(loading(0.3))
			yield()

			check(reporter.toString() == "StateFlowProgressReporter(progress=Loading(30%))")

			reporter.report(done())
			yield()
		}

		val expected = listOf(
			loading(0.0),
			loading(0.2),
			loading(0.3),
			done(),
		)

		check(expected == reporter.progress.transformWhile { emit(it); it != done() }.toList())
	}

})
