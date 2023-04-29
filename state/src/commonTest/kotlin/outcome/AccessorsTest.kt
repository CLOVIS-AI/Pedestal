package opensavvy.state.outcome

import kotlin.test.Test
import kotlin.test.assertEquals

class AccessorsTest {

    private object Failed

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
            Failed.failed().valueOrNull,
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
            Failed,
            Failed.failed().failureOrNull,
        )
    }
}
