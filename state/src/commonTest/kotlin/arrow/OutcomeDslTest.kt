package opensavvy.state.arrow

import arrow.core.left
import arrow.core.raise.either
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful
import kotlin.test.Test
import kotlin.test.assertEquals

class OutcomeDslTest {

    private data class Failed(val value: Any)

    @Test
    fun success() {
        val success = out<Failed, Int> { 2 }

        assertEquals(2.successful(), success)
    }

    @Test
    fun failure() {
        val failure = out<Failed, Int> {
            raise(Failed("test"))
        }

        assertEquals(Failed("test").failed(), failure)
    }

    @Test
    fun bindSuccess() {
        val success = out<Failed, Int> {
            Outcome.Success(2).bind()
        }

        assertEquals(2.successful(), success)
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // that's the purpose of the test!
    @Test
    fun bindFailure() {
        val failure = out<Failed, Int> {
            Outcome.Failure(Failed("test")).bind()
        }

        assertEquals(Failed("test").failed(), failure)
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // that's the purpose of the test!
    @Test
    fun toEither() {
        val result = either<Failed, Int> {
            out {
                raise(Failed("test"))
            }.toEither().bind()
        }

        assertEquals(
            Failed("test").left(),
            result,
        )
    }
}
