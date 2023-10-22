package opensavvy.progress.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.yield
import opensavvy.progress.done
import opensavvy.progress.loading
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class StateFlowProgressReporterTest {

    @Test
    fun test() = runTest {
        val reporter = StateFlowProgressReporter()

        launch {
            reporter.report(loading(0.2))
            yield()

            reporter.report(loading(0.3))
            yield()

            assertEquals("StateFlowProgressReporter(progress=Loading(30%))", reporter.toString())

            reporter.report(done())
            yield()
        }

        val expected = listOf(
            loading(0.0),
            loading(0.2),
            loading(0.3),
            done(),
        )

        assertEquals(expected, reporter.progress.transformWhile { emit(it); it != done() }.toList())
    }
}
