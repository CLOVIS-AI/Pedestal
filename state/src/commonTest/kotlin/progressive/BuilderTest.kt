package opensavvy.state.progressive

import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.ExperimentalProgressApi
import opensavvy.progress.Progressive
import opensavvy.progress.loading
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful
import kotlin.test.assertEquals

@OptIn(ExperimentalProgressApi::class)
class BuilderTest : PreparedSpec({

	@Suppress("LocalVariableName")
	val Failed = "FAILED"

	suite("With progress") {
		test("Success") {
			assertEquals(
				ProgressiveOutcome.Success(5, loading(0.57)),
				Outcome.Success(5).withProgress(loading(0.57)),
			)
		}

		test("Failure") {
			assertEquals(
				ProgressiveOutcome.Failure(Failed, loading(0.57)),
				Outcome.Failure(Failed).withProgress(loading(0.57)),
			)
		}
	}

	suite("Convert to Progressive") {
		test("Success") {
			val initial = Progressive(5.successful(), loading(0.2))

			assertEquals(
				initial,
				initial.flatten().explode(),
			)
		}

		test("Failure") {
			val initial = Progressive(3.failed(), loading(0.1))

			assertEquals(
				initial,
				initial.flatten().explode(),
			)
		}

		test("Incomplete") {
			assertEquals(
				Progressive(null, loading(0.33)),
				ProgressiveOutcome.Incomplete(loading(0.33)).explode(),
			)
		}
	}

	suite("Copy function") {

		fun ProgressiveOutcome<*, *>.copyProgress() =
			copy(progress = loading(0.23))

		test("Success") {
			assertEquals(
				ProgressiveOutcome.Success(Unit, loading(0.23)),
				ProgressiveOutcome.Success(Unit).copyProgress()
			)
		}

		test("Failure") {
			assertEquals(
				ProgressiveOutcome.Failure(Unit, loading(0.23)),
				ProgressiveOutcome.Failure(Unit).copyProgress()
			)
		}

		test("Incomplete") {
			assertEquals(
				ProgressiveOutcome.Incomplete(loading(0.23)),
				ProgressiveOutcome.Incomplete().copyProgress()
			)
		}
	}
})
