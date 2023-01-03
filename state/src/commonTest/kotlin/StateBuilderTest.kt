@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.state

import arrow.core.Either
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import opensavvy.state.outcome.ensureValid
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.out
import opensavvy.state.outcome.successful
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class StateBuilderTest {

	@Test
	fun success() = runTest {
		val data = out { 0 }

		assertEquals(successful(0), data)
	}

	@Test
	fun ensure() = runTest {
		val data = out {
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
		val data = out {
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
		val data = out {
			require(false) { "some error" }
			0
		}

		assertIs<Either.Left<*>>(data)
	}

	@Test
	fun throwIllegalStateException() = runTest {
		val data = out {
			check(false) { "some error" }
			0
		}

		assertIs<Either.Left<*>>(data)
	}

}
