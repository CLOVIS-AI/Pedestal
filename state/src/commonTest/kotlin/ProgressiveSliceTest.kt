package opensavvy.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import opensavvy.state.ProgressionReporter.Companion.report
import opensavvy.state.progressive.*
import opensavvy.state.slice.failed
import opensavvy.state.slice.successful
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class ProgressiveSliceTest {

	@Test
	fun convertSuccess() {
		val slice = successful(5)

		val progressive = slice.withProgress(Progression.loading(0.7))

		assertEquals(slice, progressive.asSlice())
		assertEquals(5, progressive.valueOrNull)
		assertEquals(Progression.loading(0.7), progressive.progress)
	}

	@Test
	fun convertFailure() {
		val slice = failed("Error", Failure.Kind.NotFound)

		val progressive = slice.withProgress(Progression.loading(0.7))

		assertEquals(slice, progressive.asSlice())
		assertEquals(Failure(Failure.Kind.NotFound, "Error"), progressive.failureOrNull)
		assertEquals(Progression.loading(0.7), progressive.progress)
	}

	@Test
	fun convertFlow() = runTest {
		assertEquals(
			listOf(
				ProgressiveSlice.Empty(Progression.loading(0.2)),
				ProgressiveSlice.Empty(Progression.loading(0.7)),
				ProgressiveSlice.Success(5),
			),
			flow {
				report(Progression.loading(0.2))
				delay(100)
				report(Progression.loading(0.7))
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
				ProgressiveSlice.Empty(Progression.loading(0.2)),
				ProgressiveSlice.Empty(Progression.loading(0.7)),
				ProgressiveSlice.Success(5),
			),
			captureProgress {
				report(Progression.loading(0.2))
				delay(100)
				report(Progression.loading(0.7))
				delay(200)
				successful(5)
			}.toList()
		)
	}

	@Test
	fun captureBuilder() = runTest {
		assertEquals(
			listOf(
				ProgressiveSlice.Empty(Progression.loading(0.2)),
				ProgressiveSlice.Empty(Progression.loading(0.7)),
				ProgressiveSlice.Success(5),
			),
			progressiveSlice {
				report(Progression.loading(0.2))
				delay(100)
				report(Progression.loading(0.7))
				delay(200)
				5
			}.toList()
		)
	}

	@Test
	fun filters() = runTest {
		assertEquals(
			successful(5),
			progressiveSlice {
				report(Progression.loading(0.2))
				delay(100)
				report(Progression.loading(0.7))
				delay(200)
				5
			}.firstValue()
		)
	}

	@Test
	fun conditionals() = runTest {
		val tests = mapOf(
			//            <slice>             to        success, failure, loading
			ProgressiveSlice.Success(5) to Triple(true, false, false),
			ProgressiveSlice.Success(5, Progression.loading()) to Triple(true, false, true),
			ProgressiveSlice.Empty() to Triple(false, false, true),
			ProgressiveSlice.Failure(Failure(Failure.Kind.NotFound, "Error")) to Triple(false, true, false),
			ProgressiveSlice.Failure(Failure(Failure.Kind.NotFound, "Error"), Progression.loading()) to Triple(
				false,
				true,
				true
			),
		)

		for ((slice, expected) in tests) {
			val (expectedSuccess, expectedFailure, expectedLoading) = expected

			var didSuccessRun = false
			var didFailureRun = false
			var didLoadingRun = false

			slice.onSuccess {
				didSuccessRun = true
			}

			slice.onFailure {
				didFailureRun = true
			}

			slice.onLoading {
				didLoadingRun = true
			}

			assertEquals(expectedSuccess, didSuccessRun)
			assertEquals(expectedFailure, didFailureRun)
			assertEquals(expectedLoading, didLoadingRun)
		}
	}

}
