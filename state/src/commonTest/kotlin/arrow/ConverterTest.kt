package opensavvy.state.arrow

import arrow.core.left
import arrow.core.right
import opensavvy.progress.loading
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.failedWithProgress
import opensavvy.state.progressive.successfulWithProgress
import kotlin.test.Test
import kotlin.test.assertEquals

class ConverterTest {

    private data class NotFound(val value: Int)

    // region To either

    @Test
    fun outcomeSuccess() {
        assertEquals(
            5.right(),
            5.successful().toEither()
        )
    }

    @Test
    fun outcomeFailure() {
        assertEquals(
            NotFound(5).left(),
            NotFound(5).failed().toEither()
        )
    }

    @Test
    fun progressiveSuccess() {
        assertEquals(
            5.right(),
            5.successfulWithProgress().toEither(),
        )
    }

    @Test
    fun progressiveFailure() {
        assertEquals(
            NotFound(5).left(),
            NotFound(5).failedWithProgress().toEither(),
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
            5.successful(),
            5.right().toOutcome()
        )
    }

    @Test
    fun toOutcomeFailure() {
        assertEquals(
            NotFound(5).failed(),
            NotFound(5).left().toOutcome()
        )
    }

    @Test
    fun toProgressiveSuccess() {
        assertEquals(
            5.successfulWithProgress(progress = loading(0.5)),
            5.right().toOutcome(progress = loading(0.5)),
        )
    }

    @Test
    fun toProgressiveFailure() {
        assertEquals(
            NotFound(5).failedWithProgress(progress = loading(0.5)),
            NotFound(5).left().toOutcome(progress = loading(0.5)),
        )
    }

    // endregion
}
