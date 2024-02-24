package opensavvy.state

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import opensavvy.prepared.runner.kotest.PreparedSpec

/**
 * This is a series of examples of how to combine state instances together.
 */
@Suppress("unused")
class ComprehensionsTest : PreparedSpec({
	test("Map values") {
		val input = flow {
			emit("5")
			delay(100)

			emit("10")
			delay(100)

			emit("test")
		}.map { it.toIntOrNull() }
			.toList()

		input shouldBe listOf(5, 10, null)
	}
})
