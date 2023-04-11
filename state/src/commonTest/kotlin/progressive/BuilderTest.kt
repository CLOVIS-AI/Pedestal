package opensavvy.state.progressive

import opensavvy.progress.loading
import opensavvy.state.failure.NotFound
import opensavvy.state.outcome.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals

class BuilderTest {

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
            ProgressiveOutcome.Failure(NotFound("test"), loading(0.57)),
            Outcome.Failure(NotFound("test")).withProgress(loading(0.57)),
        )
    }
}
