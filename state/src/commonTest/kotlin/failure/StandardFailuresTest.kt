package opensavvy.state.failure

import kotlin.test.Test
import kotlin.test.assertEquals

class StandardFailuresTest {

    @Test
    fun notFoundIdentifier() {
        assertEquals(
            5,
            NotFound(5).identifier,
        )
    }

    @Test
    fun notFoundMessage() {
        assertEquals(
            "Could not find resource '5'",
            NotFound(5).message,
        )
    }

    @Test
    fun unauthenticatedMessage() {
        assertEquals(
            "Authentication is required to access this resource",
            Unauthenticated().message,
        )
    }

    @Test
    fun unauthorizedMessage() {
        assertEquals(
            "You are not authorized to access this information",
            Unauthorized().message,
        )
    }
}
