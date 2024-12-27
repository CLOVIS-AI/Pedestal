package opensavvy.state.progressive

import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.ExperimentalProgressApi
import opensavvy.progress.Progressive
import opensavvy.progress.loading
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful

@OptIn(ExperimentalProgressApi::class)
class BuilderTest : PreparedSpec({

	@Suppress("LocalVariableName")
	val Failed = "FAILED"

	suite("With progress") {
		test("Success") {
			check(Outcome.Success(5).withProgress(loading(0.57)) == ProgressiveOutcome.Success(5, loading(0.57)))
		}

		test("Failure") {
			check(Outcome.Failure(Failed).withProgress(loading(0.57)) == ProgressiveOutcome.Failure(Failed, loading(0.57)))
		}
	}

	suite("Convert to Progressive") {
		test("Success") {
			val initial = Progressive(5.successful(), loading(0.2))

			check(initial == initial.flatten().explode())
		}

		test("Failure") {
			val initial = Progressive(3.failed(), loading(0.1))

			check(initial == initial.flatten().explode())
		}

		test("Incomplete") {
			check(ProgressiveOutcome.Incomplete(loading(0.33)).explode() == Progressive(null, loading(0.33)))
		}
	}

	suite("Copy function") {

		fun ProgressiveOutcome<*, *>.copyProgress() =
			copy(progress = loading(0.23))

		test("Success") {
			check(ProgressiveOutcome.Success(Unit).copyProgress() == ProgressiveOutcome.Success(Unit, loading(0.23)))
		}

		test("Failure") {
			check(ProgressiveOutcome.Failure(Unit).copyProgress() == ProgressiveOutcome.Failure(Unit, loading(0.23)))
		}

		test("Incomplete") {
			check(ProgressiveOutcome.Incomplete().copyProgress() == ProgressiveOutcome.Incomplete(loading(0.23)))
		}
	}
})
