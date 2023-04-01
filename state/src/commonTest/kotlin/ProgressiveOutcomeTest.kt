package opensavvy.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import opensavvy.progress.coroutines.report
import opensavvy.progress.loading
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful
import opensavvy.state.progressive.*
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressiveOutcomeTest {

	@Test
	fun convertSuccess() {
		val out = successful(5)

		val progressive = out.withProgress(loading(0.7))

		assertEquals(out, progressive.asOutcome())
		assertEquals(5, progressive.valueOrNull)
		assertEquals(loading(0.7), progressive.progress)
	}

	@Test
	fun convertFailure() {
		val out = failed("Error", Failure.Kind.NotFound)

		val progressive = out.withProgress(loading(0.7))

		assertEquals(out, progressive.asOutcome())
		assertEquals(Failure(Failure.Kind.NotFound, "Error"), progressive.failureOrNull)
		assertEquals(loading(0.7), progressive.progress)
	}

	@Test
	fun convertFlow() = runTest {
		assertEquals(
			listOf(
				ProgressiveOutcome.Empty(loading(0.2)),
				ProgressiveOutcome.Empty(loading(0.7)),
				ProgressiveOutcome.Success(5),
			),
			flow {
				report(loading(0.2))
				delay(100)
				report(loading(0.7))
				delay(200)
				emit(successful(5))
			}.captureProgress()
				.toList()
		)
	}

	@Test
	fun captureFromBlock() = runTest {
		assertEquals(
			listOf(
				ProgressiveOutcome.Empty(loading(0.2)),
				ProgressiveOutcome.Empty(loading(0.7)),
				ProgressiveOutcome.Success(5),
			),
			captureProgress {
				report(loading(0.2))
				delay(100)
				report(loading(0.7))
				delay(200)
				successful(5)
			}.toList()
		)
	}

	@Test
	fun captureBuilder() = runTest {
		assertEquals(
			listOf(
				ProgressiveOutcome.Empty(loading(0.2)),
				ProgressiveOutcome.Empty(loading(0.7)),
				ProgressiveOutcome.Success(5),
			),
			progressive {
				report(loading(0.2))
				delay(100)
				report(loading(0.7))
				delay(200)
				5
			}.toList()
		)
	}

	@Test
	fun filters() = runTest {
		assertEquals(
			successful(5),
			progressive {
				report(loading(0.2))
				delay(100)
				report(loading(0.7))
				delay(200)
				5
			}.firstValue()
		)
	}

	@Test
	fun conditionals() = runTest {
		val tests = mapOf(
			//            <outcome>             to        success, failure, loading
			ProgressiveOutcome.Success(5) to Triple(true, false, false),
			ProgressiveOutcome.Success(5, loading()) to Triple(true, false, true),
			ProgressiveOutcome.Empty() to Triple(false, false, true),
			ProgressiveOutcome.Failure(Failure(Failure.Kind.NotFound, "Error")) to Triple(false, true, false),
			ProgressiveOutcome.Failure(Failure(Failure.Kind.NotFound, "Error"), loading()) to Triple(
				false,
				true,
				true
			),
		)

		for ((out, expected) in tests) {
			val (expectedSuccess, expectedFailure, expectedLoading) = expected

			var didSuccessRun = false
			var didFailureRun = false
			var didLoadingRun = false

			out.onSuccess {
				didSuccessRun = true
			}

			out.onFailure {
				didFailureRun = true
			}

			out.onLoading {
				didLoadingRun = true
			}

			assertEquals(expectedSuccess, didSuccessRun)
			assertEquals(expectedFailure, didFailureRun)
			assertEquals(expectedLoading, didLoadingRun)
		}
	}

}
