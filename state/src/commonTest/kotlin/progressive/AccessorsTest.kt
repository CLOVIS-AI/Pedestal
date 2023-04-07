package opensavvy.state.progressive

import opensavvy.progress.loading
import opensavvy.state.failure.NotFound
import opensavvy.state.outcome.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals


class AccessorsTest {

    @Test
    fun valueOnSuccess() {
        assertEquals(
            5,
            5.success().valueOrNull,
        )
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // it's the goal of the test!
    @Test
    fun valueOnFailure() {
        assertEquals<Int?>(
            null,
            NotFound("").failed().valueOrNull,
        )
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // it's the goal of the test!
    @Test
    fun failureOnSuccess() {
        assertEquals(
            null,
            5.success().failureOrNull,
        )
    }

    @Test
    fun failureOnFailure() {
        assertEquals(
            NotFound(""),
            NotFound("").failed().failureOrNull,
        )
    }

    @Test
    fun destructurationOnSuccess() {
        val value: ProgressiveOutcome<*, Int> = ProgressiveOutcome.Success(5, loading(0.23))
        val (outcome, progress) = value

        assertEquals(Outcome.Success(5), outcome)
        assertEquals(loading(0.23), progress)
    }

    @Test
    fun destructurationOnFailure() {
        val value: ProgressiveOutcome<*, Int> = ProgressiveOutcome.Failure(NotFound("id"), loading(0.23))
        val (outcome, progress) = value

        assertEquals(Outcome.Failure(NotFound("id")), outcome)
        assertEquals(loading(0.23), progress)
    }

    @Test
    fun destructurationOnIncomplete() {
        val value: ProgressiveOutcome<*, Int> = ProgressiveOutcome.Incomplete(loading(0.23))
        val (outcome, progress) = value

        assertEquals(null, outcome)
        assertEquals(loading(0.23), progress)
    }
}
