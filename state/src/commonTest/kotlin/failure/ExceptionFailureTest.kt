package opensavvy.state.failure

import kotlin.test.Test
import kotlin.test.assertEquals

class ExceptionFailureTest {

    @Test
    fun missingMessage() {
        assertEquals(
            "No message available",
            ExceptionFailure(NotFound, RuntimeException()).message,
        )
    }

    @Test
    fun providedMessage() {
        assertEquals(
            "The selected message",
            ExceptionFailure(NotFound, RuntimeException("The selected message")).message,
        )
    }

    @Test
    fun exception() {
        assertEquals(
            RuntimeException("The selected message").message,
            RuntimeException("The selected message").asFailure(NotFound).exception.message,
        )
    }
}
