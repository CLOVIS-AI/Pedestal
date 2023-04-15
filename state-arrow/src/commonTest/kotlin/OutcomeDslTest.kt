package opensavvy.state.arrow

import arrow.core.left
import arrow.core.raise.either
import opensavvy.state.failure.Failure
import opensavvy.state.failure.NotFound
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.success
import kotlin.test.Test
import kotlin.test.assertEquals

class OutcomeDslTest {

    @Test
    fun success() {
        val success = out<Failure, Int> { 2 }

        assertEquals(2.success(), success)
    }

    @Test
    fun failure() {
        val failure = out<Failure, Int> {
            raise(NotFound("test"))
        }

        assertEquals(NotFound("test").failed(), failure)
    }

    @Test
    fun bindSuccess() {
        val success = out<Failure, Int> {
            Outcome.Success(2).bind()
        }

        assertEquals(2.success(), success)
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // that's the purpose of the test!
    @Test
    fun bindFailure() {
        val failure = out<Failure, Int> {
            Outcome.Failure(NotFound("test")).bind()
        }

        assertEquals(NotFound("test").failed(), failure)
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // that's the purpose of the test!
    @Test
    fun toEither() {
        val result = either<Failure, Int> {
            out {
                raise(NotFound("test"))
            }.toEither().bind()
        }

        assertEquals(
            NotFound("test").left(),
            result,
        )
    }
}
