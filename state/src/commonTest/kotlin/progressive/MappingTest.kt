package opensavvy.state.progressive

import opensavvy.prepared.runner.kotest.PreparedSpec

class MappingTest : PreparedSpec({

	@Suppress("LocalVariableName") val Failed = "FAILED"

	suite("map") {
		test("Success") {
			check(ProgressiveOutcome.Success(5).map { it.toString() } == "5".successfulWithProgress())
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Failure") {
			check(ProgressiveOutcome.Failure(Failed).map { it.toString() } == Failed.failedWithProgress())
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Incomplete") {
			check(ProgressiveOutcome.Incomplete().map { it.toString() } == ProgressiveOutcome.Incomplete())
		}
	}

	suite("mapFailure") {
		test("Success") {
			check(
				ProgressiveOutcome.Success(5).mapFailure {
					@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
					it.toString()
				} == 5.successfulWithProgress()
			)
		}

		test("Failure") {
			check(ProgressiveOutcome.Failure(5).mapFailure { it.toString() } == "5".failedWithProgress())
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Incomplete") {
			check(ProgressiveOutcome.Incomplete().mapFailure { it.toString() } == ProgressiveOutcome.Incomplete())
		}
	}
})
