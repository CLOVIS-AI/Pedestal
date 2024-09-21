package opensavvy.state.arrow

import arrow.core.left
import arrow.core.right
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.failedWithProgress
import opensavvy.state.progressive.successfulWithProgress
import opensavvy.state.progressive.withProgress
import kotlin.test.assertEquals

class ConverterTest : PreparedSpec({

	data class NotFound(val value: Int)

	suite("To either") {
		suite("Outcome") {

			test("Success") {
				assertEquals(
					5.right(),
					5.successful().toEither()
				)
			}

			test("Failure") {
				assertEquals(
					NotFound(5).left(),
					NotFound(5).failed().toEither()
				)
			}
		}

		suite("ProgressiveOutcome") {
			test("Success") {
				assertEquals(
					5.right(),
					5.successful().withProgress(loading(0.2)).toEither()
				)
			}

			test("Failure") {
				assertEquals(
					NotFound(5).left(),
					NotFound(5).failedWithProgress().toEither(),
				)
			}

			test("Incomplete") {
				assertEquals(
					null,
					ProgressiveOutcome.Incomplete().toEither(),
				)
			}
		}
	}

	suite("From either") {
		suite("Outcome") {
			test("Success") {
				assertEquals(
					5.successful(),
					5.right().toOutcome()
				)
			}

			test("Failure") {
				assertEquals(
					NotFound(5).failed(),
					NotFound(5).left().toOutcome()
				)
			}
		}

		suite("ProgressiveOutcome") {
			suite("Success") {
				assertEquals(
					5.successfulWithProgress(progress = loading(0.5)),
					5.right().toOutcome(progress = loading(0.5)),
				)
			}

			suite("Failure") {
				assertEquals(
					NotFound(5).failedWithProgress(progress = loading(0.5)),
					NotFound(5).left().toOutcome(progress = loading(0.5)),
				)
			}
		}
	}
})
