package opensavvy.state.coroutines

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import opensavvy.progress.coroutines.report
import opensavvy.progress.loading
import opensavvy.state.failure.Unauthorized
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureProgressTest {

    @Test
    fun captureFromFlow() = runTest {
        val actual = flow {
            report(loading(0.2))
            report(loading(0.5))
            emit(Outcome.Failure(Unauthorized()))
            report(loading(0.7))
            emit(Outcome.Success(5))
        }.captureProgress()
            .toList()

        val expect = listOf(
            ProgressiveOutcome.Incomplete(loading(0.2)),
            ProgressiveOutcome.Incomplete(loading(0.5)),
            ProgressiveOutcome.Failure(Unauthorized()),
            ProgressiveOutcome.Incomplete(loading(0.7)),
            ProgressiveOutcome.Success(5),
        )

        assertEquals(expect, actual)
    }

    @Test
    fun captureFromBlock() = runTest {
        val actual = captureProgress {
            report(loading(0.2))
            report(loading(0.7))
            Outcome.Success(5)
        }.toList()

        val expect = listOf(
            ProgressiveOutcome.Incomplete(loading(0.2)),
            ProgressiveOutcome.Incomplete(loading(0.7)),
            ProgressiveOutcome.Success(5),
        )

        assertEquals(expect, actual)
    }
}
