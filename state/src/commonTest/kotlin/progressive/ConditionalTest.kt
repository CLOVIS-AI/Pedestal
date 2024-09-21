package opensavvy.state.progressive

import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConditionalTest : PreparedSpec({

	@Suppress("LocalVariableName")
	val Failed = "FAILED"

	suite("onSuccess") {
		test("Success") {
			var test = false

			ProgressiveOutcome.Success(5).onSuccess {
				test = true
			}

			assertTrue(test)
		}

		test("Failure") {
			var test = false

			ProgressiveOutcome.Failure(Failed).onSuccess {
				test = true
			}

			assertFalse(test)
		}

		test("Incomplete") {
			var test = false

			ProgressiveOutcome.Incomplete().onSuccess {
				test = true
			}

			assertFalse(test)
		}
	}

	suite("onFailure") {
		test("Success") {
			var test = false

			ProgressiveOutcome.Success(5).onFailure {
				test = true
			}

			assertFalse(test)
		}

		test("Failure") {
			var test = false

			ProgressiveOutcome.Failure(Failed).onFailure {
				test = true
			}

			assertTrue(test)
		}

		test("Incomplete") {
			var test = false

			ProgressiveOutcome.Incomplete().onFailure {
				test = true
			}

			assertFalse(test)
		}
	}

	suite("onIncomplete") {
		test("Success") {
			var test = false

			ProgressiveOutcome.Success(5).onIncomplete {
				test = true
			}

			assertFalse(test)
		}

		test("Failure") {
			var test = false

			ProgressiveOutcome.Failure(Failed).onIncomplete {
				test = true
			}

			assertFalse(test)
		}

		test("Incomplete") {
			var test = false

			ProgressiveOutcome.Incomplete().onIncomplete {
				test = true
			}

			assertTrue(test)
		}
	}

	suite("onLoading") {
		test("Done") {
			var test = false

			ProgressiveOutcome.Success(5).onLoading {
				test = true
			}

			assertFalse(test)
		}

		test("Loading") {
			var test = false

			ProgressiveOutcome.Failure(Failed, loading(0.2)).onLoading {
				test = true
			}

			assertTrue(test)
		}

		test("Incomplete") {
			var test = false

			ProgressiveOutcome.Incomplete().onLoading {
				test = true
			}

			assertTrue(test)
		}
	}
})
