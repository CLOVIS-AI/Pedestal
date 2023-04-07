package opensavvy.state.outcome

import opensavvy.state.failure.NotFound
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConditionalTest {

    @Test
    fun success_onSuccess() {
        var test = false

        Outcome.Success(5).onSuccess {
            test = true
        }

        assertTrue(test)
    }

    @Test
    fun failure_onSuccess() {
        var test = false

        Outcome.Failure(NotFound(5)).onSuccess {
            test = true
        }

        assertFalse(test)
    }

    @Test
    fun success_onFailure() {
        var test = false

        Outcome.Success(5).onFailure {
            test = true
        }

        assertFalse(test)
    }

    @Test
    fun failure_onFailure() {
        var test = false

        Outcome.Failure(NotFound(5)).onFailure {
            test = true
        }

        assertTrue(test)
    }
}
