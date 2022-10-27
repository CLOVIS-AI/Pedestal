@file:OptIn(ExperimentalCoroutinesApi::class, ExperimentalCoroutinesApi::class)

package opensavvy.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * This is a series of examples of how to combine state instances together.
 */
class ComprehensionsTest {

	private fun strings() = flow {
		emit("5")
		delay(100)
		emit("10")
		delay(100)
		emit("wtf")
	}

	@Test
	fun mapValue() = runTest {
		val results = strings()
			.map { it.toIntOrNull() }
			.toList()

		val expected = listOf(
			5,
			10,
			null,
		)

		assertEquals(expected, results)
	}

}
