@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import opensavvy.state.Slice.Companion.failed
import opensavvy.state.Slice.Companion.pending
import opensavvy.state.Slice.Companion.successful
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * This is a series of examples of how to combine state instances together.
 */
class ComprehensionsTest {

	private fun decodeStringOrFailed(str: String): Slice<Int> {
		val decoded = str.toIntOrNull()
			?: return failed(Status.StandardFailure.Kind.Invalid, "The passed string is not an integer: '$str'")
		return successful(decoded)
	}

	private fun decodeString(str: String) = state {
		emit(pending(0.0))
		delay(10)
		emit(decodeStringOrFailed(str))
	}

	private fun strings() = state {
		emit(successful("5", Progression.loading(0.0)))
		delay(100)
		emit(successful("10", Progression.loading(0.5)))
		delay(100)
		emit(successful("wtf", Progression.done()))
	}

	@Test
	fun mapValue() = runTest {
		val results = strings()
			.mapSuccess { it.toIntOrNull() }
			.toList()

		val expected = listOf(
			successful(5, Progression.loading(0.0)),
			successful(10, Progression.loading(0.5)),
			successful(null, Progression.done())
		)

		assertEquals(expected, results)
	}

	/**
	 * For each string instance, decode it into an integer if possible.
	 */
	@Test
	fun mapSlice() = runTest {
		val results = strings()
			.mapSuccessSlice { decodeStringOrFailed(it) }
			.toList()

		val expected = listOf(
			successful(5),
			successful(10),
			failed(Status.StandardFailure.Kind.Invalid, "The passed string is not an integer: 'wtf'"),
		)

		assertEquals(expected, results)
	}

	@Test
	fun oneToMany() = runTest {
		val results = strings()
			.flatMapSuccess {
				emitAll(decodeString(it))
			}
			.toList()

		val expected = listOf(
			pending(0.0),
			successful(5),
			pending(0.0),
			successful(10),
			pending(0.0),
			failed(Status.StandardFailure.Kind.Invalid, "The passed string is not an integer: 'wtf'"),
		)

		assertEquals(expected, results)
	}

}
