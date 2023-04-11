package opensavvy.state.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import opensavvy.progress.loading
import opensavvy.state.failure.NotFound
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressiveFlowAccessorsTest {

    @Test
    fun failure() = runTest {
        val input = flowOf(
            ProgressiveOutcome.Incomplete(loading(0.3)),
            ProgressiveOutcome.Failure(NotFound(2), loading(0.4)),
            ProgressiveOutcome.Failure(NotFound(2)),
        )

        assertEquals(
            Outcome.Failure(NotFound(2)),
            input.firstValue(),
        )
    }

    @Test
    fun success() = runTest {
        val input = flowOf(
            ProgressiveOutcome.Incomplete(loading(0.3)),
            ProgressiveOutcome.Failure(NotFound(2), loading(0.4)),
            ProgressiveOutcome.Success(2),
        )

        assertEquals(
            Outcome.Success(2),
            input.firstValue(),
        )
    }
}
