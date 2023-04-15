package opensavvy.state.failure

import kotlin.test.Test
import kotlin.test.assertEquals

class BasicFailureTest {

    @Test
    fun minimalEquals() {
        assertEquals(
            BasicFailure(NotFound, "message"),
            BasicFailure(NotFound, "message"),
        )
    }

    @Test
    fun causeEquals() {
        val cause = Unauthenticated()

        assertEquals(
            BasicFailure(NotFound, "message", cause),
            BasicFailure(NotFound, "message", cause),
        )
    }

    @Test
    fun minimalToString() {
        assertEquals(
            "NotFound: this is the message".trimIndent(),
            BasicFailure(NotFound, "this is the message").toString(),
        )
    }

    @Test
    fun causeToString() {
        assertEquals(
            """
                NotFound: this is the message
                Caused by Unauthenticated: original message
            """.trimIndent(),
            BasicFailure(
                NotFound,
                "this is the message",
                cause = BasicFailure(Unauthenticated, "original message")
            ).toString(),
        )
    }

}
