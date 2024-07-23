package opensavvy.state.progressive

import opensavvy.progress.loading
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.outcome.Outcome
import kotlin.test.Test
import kotlin.test.assertEquals


class AccessorsTest {

    private object Failed

    // region Unsuccessful

    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Test
    fun unsuccessfulProgressFailure() {
        val failure = ProgressiveOutcome.Failure(Unit, loading(0.13)) as ProgressiveOutcome.Unsuccessful<Unit>
        assertEquals(
            loading(0.13),
            failure.progress
        )
    }

    @OptIn(ExperimentalProgressiveRaiseApi::class)
    @Test
    fun unsuccessfulProgressIncomplete() {
        val failure = ProgressiveOutcome.Incomplete(loading(0.13)) as ProgressiveOutcome.Unsuccessful<Unit>
        assertEquals(
            loading(0.13),
            failure.progress
        )
    }

    // endregion
    // region Get or null

    @Test
    fun valueOnSuccess() {
        assertEquals(
            5,
            5.successfulWithProgress().valueOrNull,
        )
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // it's the goal of the test!
    @Test
    fun valueOnFailure() {
        assertEquals<Int?>(
            null,
            Failed.failedWithProgress().valueOrNull,
        )
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // it's the goal of the test!
    @Test
    fun failureOnSuccess() {
        assertEquals(
            null,
            5.successfulWithProgress().failureOrNull,
        )
    }

    @Test
    fun failureOnFailure() {
        assertEquals(
            Failed,
            Failed.failedWithProgress().failureOrNull,
        )
    }

    // endregion
    // region Nothing variant

    @Test
    fun valueOnSuccessNothing() {
        assertEquals(
            5,
            (5.successfulWithProgress() as ProgressiveOutcome<Nothing, Int>).value,
        )
    }

    @Test
    fun failureOnFailureNothing() {
        assertEquals(
            Failed,
            (Failed.failedWithProgress() as ProgressiveOutcome<Failed, Nothing>).failure,
        )
    }

    // endregion
    // region Destructuration

    @Test
    fun destructurationOnSuccess() {
        val value: ProgressiveOutcome<*, Int> = ProgressiveOutcome.Success(5, loading(0.23))
        val (outcome, progress) = value

        assertEquals(Outcome.Success(5), outcome)
        assertEquals(loading(0.23), progress)
    }

    @Test
    fun destructurationOnFailure() {
        val value: ProgressiveOutcome<*, Int> = ProgressiveOutcome.Failure(Failed, loading(0.23))
        val (outcome, progress) = value

        assertEquals(Outcome.Failure(Failed), outcome)
        assertEquals(loading(0.23), progress)
    }

    @Test
    fun destructurationOnIncomplete() {
        val value: ProgressiveOutcome<*, Int> = ProgressiveOutcome.Incomplete(loading(0.23))
        val (outcome, progress) = value

        assertEquals(null, outcome)
        assertEquals(loading(0.23), progress)
    }

    // endregion
}
