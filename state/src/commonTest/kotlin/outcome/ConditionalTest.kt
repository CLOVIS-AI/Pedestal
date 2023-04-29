package opensavvy.state.outcome

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConditionalTest {

    private object Failed

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

        Outcome.Failure(Failed).onSuccess {
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

        Outcome.Failure(Failed).onFailure {
            test = true
        }

        assertTrue(test)
    }
}
