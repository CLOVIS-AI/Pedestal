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

class ConverterTest : PreparedSpec({

	data class NotFound(val value: Int)

	suite("To either") {
		suite("Outcome") {

			test("Success") {
				check(5.successful().toEither() == 5.right())
			}

			test("Failure") {
				check(NotFound(5).failed().toEither() == NotFound(5).left())
			}
		}

		suite("ProgressiveOutcome") {
			test("Success") {
				check(5.successful().withProgress(loading(0.2)).toEither() == 5.right())
			}

			test("Failure") {
				check(NotFound(5).failedWithProgress().toEither() == NotFound(5).left())
			}

			test("Incomplete") {
				check(ProgressiveOutcome.Incomplete().toEither() == null)
			}
		}
	}

	suite("From either") {
		suite("Outcome") {
			test("Success") {
				check(5.right().toOutcome() == 5.successful())
			}

			test("Failure") {
				check(NotFound(5).left().toOutcome() == NotFound(5).failed())
			}
		}

		suite("ProgressiveOutcome") {
			suite("Success") {
				check(5.right().toOutcome(loading(0.5)) == 5.successfulWithProgress(loading(0.5)))
			}

			suite("Failure") {
				check(NotFound(5).left().toOutcome(loading(0.5)) == NotFound(5).failedWithProgress(loading(0.5)))
			}
		}
	}
})
