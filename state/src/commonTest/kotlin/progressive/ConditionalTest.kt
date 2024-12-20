package opensavvy.state.progressive

import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading

class ConditionalTest : PreparedSpec({

	@Suppress("LocalVariableName")
	val Failed = "FAILED"

	suite("onSuccess") {
		test("Success") {
			var test = false

			ProgressiveOutcome.Success(5).onSuccess {
				test = true
			}

			check(test)
		}

		test("Failure") {
			var test = false

			ProgressiveOutcome.Failure(Failed).onSuccess {
				test = true
			}

			check(!test)
		}

		test("Incomplete") {
			var test = false

			ProgressiveOutcome.Incomplete().onSuccess {
				test = true
			}

			check(!test)
		}
	}

	suite("onFailure") {
		test("Success") {
			var test = false

			ProgressiveOutcome.Success(5).onFailure {
				test = true
			}

			check(!test)
		}

		test("Failure") {
			var test = false

			ProgressiveOutcome.Failure(Failed).onFailure {
				test = true
			}

			check(test)
		}

		test("Incomplete") {
			var test = false

			ProgressiveOutcome.Incomplete().onFailure {
				test = true
			}

			check(!test)
		}
	}

	suite("onIncomplete") {
		test("Success") {
			var test = false

			ProgressiveOutcome.Success(5).onIncomplete {
				test = true
			}

			check(!test)
		}

		test("Failure") {
			var test = false

			ProgressiveOutcome.Failure(Failed).onIncomplete {
				test = true
			}

			check(!test)
		}

		test("Incomplete") {
			var test = false

			ProgressiveOutcome.Incomplete().onIncomplete {
				test = true
			}

			check(test)
		}
	}

	suite("onLoading") {
		test("Done") {
			var test = false

			ProgressiveOutcome.Success(5).onLoading {
				test = true
			}

			check(!test)
		}

		test("Loading") {
			var test = false

			ProgressiveOutcome.Failure(Failed, loading(0.2)).onLoading {
				test = true
			}

			check(test)
		}

		test("Incomplete") {
			var test = false

			ProgressiveOutcome.Incomplete().onLoading {
				test = true
			}

			check(test)
		}
	}
})
