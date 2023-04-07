package opensavvy.state.arrow

import arrow.core.left
import arrow.core.right
import opensavvy.state.failure.NotFound
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import opensavvy.state.outcome.failed as outcomeFailed
import opensavvy.state.outcome.success as outcomeSuccess
import opensavvy.state.progressive.failed as progressiveFailed
import opensavvy.state.progressive.success as progressiveSuccess

class ConverterTest {

    @Test
    fun outcomeSuccess() {
        assertEquals(
            5.right(),
            5.outcomeSuccess().toEither()
        )
    }

    @Test
    fun outcomeFailure() {
        assertEquals(
            NotFound(5).left(),
            NotFound(5).outcomeFailed().toEither()
        )
    }

    @Test
    fun progressiveSuccess() {
        assertEquals(
            5.right(),
            5.progressiveSuccess().toEither(),
        )
    }

    @Test
    fun progressiveFailure() {
        assertEquals(
            NotFound(5).left(),
            NotFound(5).progressiveFailed().toEither(),
        )
    }

    @Test
    fun progressiveIncomplete() {
        assertEquals(
            null,
            ProgressiveOutcome.Incomplete().toEither(),
        )
    }
}
