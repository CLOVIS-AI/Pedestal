package opensavvy.state.outcome

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorsTest {

    private object Failed

    // region variant orNull

    @Test
    fun valueOnSuccess() {
        assertEquals(
            5,
            5.successful().valueOrNull,
        )
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // it's the goal of the test!
    @Test
    fun valueOnFailure() {
        assertEquals<Int?>(
            null,
            Failed.failed().valueOrNull,
        )
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // it's the goal of the test!
    @Test
    fun failureOnSuccess() {
        assertEquals(
            null,
            5.successful().failureOrNull,
        )
    }

    @Test
    fun failureOnFailure() {
        assertEquals(
            Failed,
            Failed.failed().failureOrNull,
        )
    }

    // endregion
    // region variant Nothing

    @Test
    fun valueOnSuccessNothing() {
        assertEquals(
            5,
            (5.successful() as Outcome<Nothing, Int>).value,
        )
    }

    @Test
    fun failureOnFailureNothing() {
        assertEquals(
            Failed,
            (Failed.failed() as Outcome<Failed, Nothing>).failure,
        )
    }

    // endregion
}
