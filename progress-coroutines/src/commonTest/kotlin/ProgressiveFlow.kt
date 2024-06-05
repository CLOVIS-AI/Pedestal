package opensavvy.progress.coroutines

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.toList
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.ExperimentalProgressApi
import opensavvy.progress.Progressive
import opensavvy.progress.done
import opensavvy.progress.loading

@OptIn(ExperimentalProgressApi::class)
class ProgressiveFlow : PreparedSpec({

	suite("Capture progress") {

		test("Capture in a function") {
			captureProgress {
				report(loading(0.1))
				delay(100)
				report(loading(0.9))
				"It's done"
			}.toList() shouldBe listOf(
				Progressive(null, loading(0.1)),
				Progressive(null, loading(0.9)),
				Progressive("It's done", done()),
			)
		}

		test("Capture in a flow") {
			flow {
				delay(100)
				report(loading(0.5))
				emit("It's done")
			}
				.onStart { report(loading(0.0)) }
				.onEach { report(loading(0.99)) }
				.captureProgress()
				.toList()
				.shouldBe(
					listOf(
						Progressive(null, loading(0.0)),
						Progressive(null, loading(0.5)),
						Progressive(null, loading(0.99)),
						Progressive("It's done", done()),
					)
				)
		}

		test("Ignore non-loading events") {
			captureProgress {
				report(loading(0.1))
				report(done())
				"It's done"
			}.toList() shouldBe listOf(
				Progressive(null, loading(0.1)),
				Progressive("It's done", done()),
			)
		}

	}

})
