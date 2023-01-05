@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.state

import arrow.core.Either
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import opensavvy.state.slice.ensureValid
import opensavvy.state.slice.failed
import opensavvy.state.slice.slice
import opensavvy.state.slice.successful
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StateBuilderTest {

	@Test
	fun success() = runTest {
		val data = slice { 0 }

		assertEquals(successful(0), data)
	}

	@Test
	fun ensure() = runTest {
		val data = slice {
			ensureValid(false) { "this is always invalid" }
			0
		}

		assertEquals(
			failed("this is always invalid", Failure.Kind.Invalid),
			data
		)
	}

	@Test
	fun throwStandard() = runTest {
		val data = slice {
			throw Failure(Failure.Kind.Invalid, "this is always invalid").toException()
			@Suppress("UNREACHABLE_CODE") 0
		}

		assertEquals(
			failed("this is always invalid", Failure.Kind.Invalid),
			data
		)
	}

	@Test
	fun throwIllegalArgumentException() = runTest {
		val data = slice {
			require(false) { "some error" }
			0
		}

		assertIs<Either.Left<*>>(data)
	}

	@Test
	fun throwIllegalStateException() = runTest {
		val data = slice {
			check(false) { "some error" }
			0
		}

		assertIs<Either.Left<*>>(data)
	}

}
