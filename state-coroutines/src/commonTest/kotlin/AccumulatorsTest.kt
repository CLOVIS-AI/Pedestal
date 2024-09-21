package opensavvy.state.coroutines

import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading
import opensavvy.state.progressive.ProgressiveOutcome
import opensavvy.state.progressive.combineCompleted
import kotlin.test.assertEquals

class AccumulatorsTest : PreparedSpec({

	test("Flow") {
		val initial = listOf(
			ProgressiveOutcome.Incomplete(loading()),
			ProgressiveOutcome.Incomplete(loading(0.0)),
			ProgressiveOutcome.Incomplete(loading(0.9)),
			ProgressiveOutcome.Success(1),
			ProgressiveOutcome.Success(1, loading()),
			ProgressiveOutcome.Success(1, loading(0.2)),
			ProgressiveOutcome.Success(2),
			ProgressiveOutcome.Success(2, loading(0.7)),
			ProgressiveOutcome.Success(3, loading(0.1)),
			ProgressiveOutcome.Incomplete(loading(0.3)),
			ProgressiveOutcome.Failure(Unit),
			ProgressiveOutcome.Incomplete(loading(0.2)),
			ProgressiveOutcome.Failure(Unit, loading(0.3)),
		)

		// The Flow version is just a copy of the Iterable version
		// We assume the Iterable version is correct (it has its own tests),
		// so we just check that the Flow version behaves the same.

		val expected = initial.combineCompleted().toList()

		val actual = initial.asFlow().combineCompleted().toList()

		assertEquals(expected, actual)
	}
})
