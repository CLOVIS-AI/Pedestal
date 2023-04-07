package opensavvy.state.progressive

import opensavvy.state.failure.NotFound
import kotlin.test.Test
import kotlin.test.assertEquals

class MappingTest {

    @Test
    fun success_map() {
        assertEquals(
            ProgressiveOutcome.Success("5"),
            ProgressiveOutcome.Success(5).map { it.toString() },
        )
    }

    @Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
    @Test
    fun failure_map() {
        assertEquals(
            ProgressiveOutcome.Failure(NotFound(5)),
            ProgressiveOutcome.Failure(NotFound(5)).map { it.toString() },
        )
    }

    @Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
    @Test
    fun incomplete_map() {
        assertEquals(
            ProgressiveOutcome.Incomplete(),
            ProgressiveOutcome.Incomplete().map { it.toString() },
        )
    }

}
