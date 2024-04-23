package opensavvy.state.arrow

import arrow.core.raise.recover
import opensavvy.progress.loading
import opensavvy.state.ExperimentalProgressiveRaiseApi
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.failedWithProgress
import opensavvy.state.progressive.successfulWithProgress
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalProgressiveRaiseApi::class)
class ProgressiveOutcomeDslTest {

    private data class Failed(val value: Any)

    @Test
    fun success() {
        val success = progressive<Failed, Int> { 2 }

        assertEquals(2.successfulWithProgress(), success)
    }

    @Test
    fun failure() {
        val failure = progressive<Failed, Int> {
            raise(Failed("test"))
        }

        assertEquals(Failed("test").failedWithProgress(), failure)
    }

    @Test
    fun bindSuccess() {
        val success = progressive<Failed, Int> {
            Outcome.Success(2).bind()
        }

        assertEquals(2.successfulWithProgress(), success)
    }

    @Suppress("IMPLICIT_NOTHING_TYPE_ARGUMENT_IN_RETURN_POSITION") // that's the purpose of the test!
    @Test
    fun bindFailure() {
        val failure = progressive<Failed, Int> {
            Outcome.Failure(Failed("test")).bind()
        }

        assertEquals(Failed("test").failedWithProgress(), failure)
    }

    @Test
    fun tricky1() {
        val result = progressive {
            recover<ProgressiveOutcome.Unsuccessful<String>, _>(
                block = {
                    raise("foo", loading(0.2))
                },
                recover = {
                    1
                }
            )
        }

        // Ideally, we'd want this to be true:
        // assertEquals(1.successfulWithProgress(), result)
        // …however, for now, we can't, so we settle for this weird behavior:
        assertEquals("foo".failedWithProgress(loading(0.2)), result)
    }
}
