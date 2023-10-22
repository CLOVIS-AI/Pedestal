package opensavvy.state.progressive

import opensavvy.progress.loading
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConditionalTest {

    private object Failed

    @Test
    fun success_onSuccess() {
        var test = false

        ProgressiveOutcome.Success(5).onSuccess {
            test = true
        }

        assertTrue(test)
    }

    @Test
    fun failure_onSuccess() {
        var test = false

        ProgressiveOutcome.Failure(Failed).onSuccess {
            test = true
        }

        assertFalse(test)
    }

    @Test
    fun incomplete_onSuccess() {
        var test = false

        ProgressiveOutcome.Incomplete().onSuccess {
            test = true
        }

        assertFalse(test)
    }

    @Test
    fun success_onFailure() {
        var test = false

        ProgressiveOutcome.Success(5).onFailure {
            test = true
        }

        assertFalse(test)
    }

    @Test
    fun failure_onFailure() {
        var test = false

        ProgressiveOutcome.Failure(Failed).onFailure {
            test = true
        }

        assertTrue(test)
    }

    @Test
    fun incomplete_onFailure() {
        var test = false

        ProgressiveOutcome.Incomplete().onFailure {
            test = true
        }

        assertFalse(test)
    }

    @Test
    fun success_onIncomplete() {
        var test = false

        ProgressiveOutcome.Success(5).onIncomplete {
            test = true
        }

        assertFalse(test)
    }

    @Test
    fun failure_onIncomplete() {
        var test = false

        ProgressiveOutcome.Failure(Failed).onIncomplete {
            test = true
        }

        assertFalse(test)
    }

    @Test
    fun incomplete_onIncomplete() {
        var test = false

        ProgressiveOutcome.Incomplete().onIncomplete {
            test = true
        }

        assertTrue(test)
    }

    @Test
    fun done_onLoading() {
        var test = false

        ProgressiveOutcome.Success(5).onLoading {
            test = true
        }

        assertFalse(test)
    }

    @Test
    fun loading_onLoading() {
        var test = false

        ProgressiveOutcome.Failure(Failed, loading(0.2)).onLoading {
            test = true
        }

        assertTrue(test)
    }

    @Test
    fun incomplete_onLoading() {
        var test = false

        ProgressiveOutcome.Incomplete().onLoading {
            test = true
        }

        assertTrue(test)
    }

}
