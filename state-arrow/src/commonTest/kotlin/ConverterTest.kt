package opensavvy.state.arrow

import arrow.core.left
import arrow.core.right
import opensavvy.progress.loading
import opensavvy.state.progressive.ProgressiveOutcome
import kotlin.test.Test
import kotlin.test.assertEquals
import opensavvy.state.outcome.failed as outcomeFailed
import opensavvy.state.outcome.success as outcomeSuccess
import opensavvy.state.progressive.failed as progressiveFailed
import opensavvy.state.progressive.success as progressiveSuccess

class ConverterTest {

    // region To either

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

    // endregion
    // region From either

    @Test
    fun toOutcomeSuccess() {
        assertEquals(
            5.outcomeSuccess(),
            5.right().toOutcome()
        )
    }

    @Test
    fun toOutcomeFailure() {
        assertEquals(
            NotFound(5).outcomeFailed(),
            NotFound(5).left().toOutcome()
        )
    }

    @Test
    fun toProgressiveSuccess() {
        assertEquals(
            5.progressiveSuccess(progress = loading(0.5)),
            5.right().toOutcome(progress = loading(0.5)),
        )
    }

    @Test
    fun toProgressiveFailure() {
        assertEquals(
            NotFound(5).progressiveFailed(progress = loading(0.5)),
            NotFound(5).left().toOutcome(progress = loading(0.5)),
        )
    }

    // endregion
}
