package opensavvy.state.progressive

import opensavvy.progress.ExperimentalProgressApi
import opensavvy.progress.Progressive
import opensavvy.progress.loading
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalProgressApi::class)
class BuilderTest {

    private object Failed

    @Test
    fun withProgressSuccess() {
        assertEquals(
            ProgressiveOutcome.Success(5, loading(0.57)),
            Outcome.Success(5).withProgress(loading(0.57)),
        )
    }

    @Test
    fun withProgressFailure() {
        assertEquals(
            ProgressiveOutcome.Failure(Failed, loading(0.57)),
            Outcome.Failure(Failed).withProgress(loading(0.57)),
        )
    }

    @Test
    fun convertSuccessToProgressive() {
        val initial = Progressive(5.successful(), loading(0.2))

        assertEquals(
            initial,
            initial.flatten().explode(),
        )
    }

    @Test
    fun convertFailureToProgressive() {
        val initial = Progressive(3.failed(), loading(0.1))

        assertEquals(
            initial,
            initial.flatten().explode(),
        )
    }

    @Test
    fun convertIncompleteToProgressive() {
        assertEquals(
            Progressive(null, loading(0.33)),
            ProgressiveOutcome.Incomplete(loading(0.33)).explode(),
        )
    }
}
