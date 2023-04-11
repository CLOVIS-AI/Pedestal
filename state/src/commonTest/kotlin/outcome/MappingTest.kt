package opensavvy.state.outcome

import opensavvy.state.failure.NotFound
import kotlin.test.Test
import kotlin.test.assertEquals

class MappingTest {

    @Test
    fun success_map() {
        assertEquals(
            Outcome.Success("5"),
            Outcome.Success(5).map { it.toString() },
        )
    }

    @Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
    @Test
    fun failure_map() {
        assertEquals(
            Outcome.Failure(NotFound(5)),
            Outcome.Failure(NotFound(5)).map { it.toString() },
        )
    }

}
