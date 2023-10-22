package opensavvy.state.progressive

import kotlin.test.Test
import kotlin.test.assertEquals

class MappingTest {

    private object Failed

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
            ProgressiveOutcome.Failure(Failed),
            ProgressiveOutcome.Failure(Failed).map { it.toString() },
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

    @Test
    fun success_mapFailure() {
        assertEquals(
            ProgressiveOutcome.Success(5),
            ProgressiveOutcome.Success(5).mapFailure {
                @Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
                it.toString()
            },
        )
    }

    @Test
    fun failure_mapFailure() {
        assertEquals(
            ProgressiveOutcome.Failure("5"),
            ProgressiveOutcome.Failure(5).mapFailure { it.toString() },
        )
    }

    @Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
    @Test
    fun incomplete_mapFailure() {
        assertEquals(
            ProgressiveOutcome.Incomplete(),
            ProgressiveOutcome.Incomplete().mapFailure { it.toString() },
        )
    }

}
