package opensavvy.state.progressive

import opensavvy.prepared.runner.kotest.PreparedSpec
import kotlin.test.assertEquals

class MappingTest : PreparedSpec({

	@Suppress("LocalVariableName") val Failed = "FAILED"

	suite("map") {
		test("Success") {
			assertEquals(
				ProgressiveOutcome.Success("5"),
				ProgressiveOutcome.Success(5).map { it.toString() },
			)
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Failure") {
			assertEquals(
				ProgressiveOutcome.Failure(Failed),
				ProgressiveOutcome.Failure(Failed).map { it.toString() },
			)
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Incomplete") {
			assertEquals(
				ProgressiveOutcome.Incomplete(),
				ProgressiveOutcome.Incomplete().map { it.toString() },
			)
		}
	}

	suite("mapFailure") {
		test("Success") {
			assertEquals(
				ProgressiveOutcome.Success(5),
				ProgressiveOutcome.Success(5).mapFailure {
					@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
					it.toString()
				},
			)
		}

		test("Failure") {
			assertEquals(
				ProgressiveOutcome.Failure("5"),
				ProgressiveOutcome.Failure(5).mapFailure { it.toString() },
			)
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Incomplete") {
			assertEquals(
				ProgressiveOutcome.Incomplete(),
				ProgressiveOutcome.Incomplete().mapFailure { it.toString() },
			)
		}
	}
})
