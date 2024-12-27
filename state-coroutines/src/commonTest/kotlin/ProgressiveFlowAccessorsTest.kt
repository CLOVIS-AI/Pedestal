package opensavvy.state.coroutines

import kotlinx.coroutines.flow.flowOf
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading
import opensavvy.state.outcome.Outcome
import opensavvy.state.progressive.ProgressiveOutcome

class ProgressiveFlowAccessorsTest : PreparedSpec({

	data class NotFound(val value: Int)

	test("Failure") {
		val input = flowOf(
			ProgressiveOutcome.Incomplete(loading(0.3)),
			ProgressiveOutcome.Failure(NotFound(2), loading(0.4)),
			ProgressiveOutcome.Failure(NotFound(2)),
		)

		check(input.now() == Outcome.Failure(NotFound(2)))
	}

	test("Success") {
		val input = flowOf(
			ProgressiveOutcome.Incomplete(loading(0.3)),
			ProgressiveOutcome.Failure(NotFound(2), loading(0.4)),
			ProgressiveOutcome.Success(2),
		)

		check(input.now() == Outcome.Success(2))
	}
})
