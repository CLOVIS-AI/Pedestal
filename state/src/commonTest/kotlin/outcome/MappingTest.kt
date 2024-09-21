package opensavvy.state.outcome

import opensavvy.prepared.runner.kotest.PreparedSpec

class MappingTest : PreparedSpec({

	suite("map") {
		test("Success") {
			check(5.successful().map { it.toString() } == "5".successful())
		}

		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Failure") {
			check(5.failed().map { it.toString() } == 5.failed())
		}
	}

	suite("mapFailure") {
		@Suppress("UNREACHABLE_CODE") // it's the purpose of the test!
		test("Success") {
			check(5.successful().mapFailure { it.toString() } == 5.successful())
		}

		test("Failure") {
			check(5.failed().mapFailure { it.toString() } == "5".failed())
		}
	}
})
