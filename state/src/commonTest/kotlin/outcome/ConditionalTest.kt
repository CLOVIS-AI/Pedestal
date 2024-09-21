package opensavvy.state.outcome

import opensavvy.prepared.runner.kotest.PreparedSpec

class ConditionalTest : PreparedSpec({

	@Suppress("LocalVariableName")
	val Failed = "FAILED"

	suite("onSuccess") {
		test("Success") {
			var test = false

			Outcome.Success(5).onSuccess {
				test = true
			}

			check(test)
		}

		test("Failure") {
			var test = false

			Outcome.Failure(Failed).onSuccess {
				test = true
			}

			check(!test)
		}
	}

	suite("onFailure") {
		test("Success") {
			var test = false

			Outcome.Success(5).onFailure {
				test = true
			}

			check(!test)
		}

		test("Failure") {
			var test = false

			Outcome.Failure(Failed).onFailure {
				test = true
			}

			check(test)
		}
	}
})
