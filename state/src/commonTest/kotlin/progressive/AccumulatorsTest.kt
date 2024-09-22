package opensavvy.state.progressive

import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading
import kotlin.test.assertEquals

class AccumulatorsTest : PreparedSpec({

	test("Iterable") {
		val actual = listOf(
			ProgressiveOutcome.Incomplete(loading(0.0)),
			ProgressiveOutcome.Incomplete(loading(0.9)),
			ProgressiveOutcome.Success(1),
			ProgressiveOutcome.Success(1, loading(0.2)),
			ProgressiveOutcome.Incomplete(loading(0.3)),
			ProgressiveOutcome.Failure(Unit),
			ProgressiveOutcome.Incomplete(loading(0.2)),
			ProgressiveOutcome.Failure(Unit, loading(0.3)),
		).combineCompleted()
			.toList()

		val expected = listOf(
			ProgressiveOutcome.Incomplete(loading(0.0)),
			ProgressiveOutcome.Incomplete(loading(0.9)),
			ProgressiveOutcome.Success(1),
			ProgressiveOutcome.Success(1, loading(0.2)),
			ProgressiveOutcome.Success(1, loading(0.3)),
			ProgressiveOutcome.Failure(Unit),
			ProgressiveOutcome.Failure(Unit, loading(0.2)),
			ProgressiveOutcome.Failure(Unit, loading(0.3)),
		)

		assertEquals(expected, actual)
	}

	test("Sequence") {
		val actual = sequenceOf(
			ProgressiveOutcome.Incomplete(loading(0.0)),
			ProgressiveOutcome.Incomplete(loading(0.9)),
			ProgressiveOutcome.Success(1),
			ProgressiveOutcome.Success(1, loading(0.2)),
			ProgressiveOutcome.Incomplete(loading(0.3)),
			ProgressiveOutcome.Failure(Unit),
			ProgressiveOutcome.Incomplete(loading(0.2)),
			ProgressiveOutcome.Failure(Unit, loading(0.3)),
		)
			.combineCompleted()
			.toList()

		val expected = listOf(
			ProgressiveOutcome.Incomplete(loading(0.0)),
			ProgressiveOutcome.Incomplete(loading(0.9)),
			ProgressiveOutcome.Success(1),
			ProgressiveOutcome.Success(1, loading(0.2)),
			ProgressiveOutcome.Success(1, loading(0.3)),
			ProgressiveOutcome.Failure(Unit),
			ProgressiveOutcome.Failure(Unit, loading(0.2)),
			ProgressiveOutcome.Failure(Unit, loading(0.3)),
		)

		assertEquals(expected, actual)
	}
})
