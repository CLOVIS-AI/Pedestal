package opensavvy.state.outcome

import kotlin.test.Test
import kotlin.test.assertEquals

class MappingTest {

    private object Failed

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
            Outcome.Failure(Failed),
            Outcome.Failure(Failed).map { it.toString() },
        )
    }

    @Test
    fun success_mapFailure() {
        assertEquals(
            Outcome.Success(5),
            Outcome.Success(5).mapFailure {
                @Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
                it.toString()
            }
        )
    }

    @Test
    fun failure_mapFailure() {
        assertEquals(
            Outcome.Failure("5"),
            Outcome.Failure(5).mapFailure { it.toString() }
        )
    }

}
