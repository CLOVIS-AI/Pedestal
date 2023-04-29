package opensavvy.state.progressive

import opensavvy.progress.loading
import opensavvy.state.outcome.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals

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
}
