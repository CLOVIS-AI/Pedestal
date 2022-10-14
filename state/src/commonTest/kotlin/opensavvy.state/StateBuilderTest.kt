@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import opensavvy.state.Slice.Companion.failed
import opensavvy.state.Slice.Companion.successful
import kotlin.test.Test
import kotlin.test.assertEquals

class StateBuilderTest {

	@Test
	fun success() = runTest {
		val data = state {
			emit(successful(0))
		}

		assertEquals(successful(0), data.firstResult())
	}

	@Test
	fun ensure() = runTest {
		val data = state<Int> {
			ensureValid(false) { "this is always invalid" }
		}

		assertEquals(
			failed(Status.StandardFailure.Kind.Invalid, "this is always invalid"),
			data.firstResult()
		)
	}

	@Test
	fun throwStandard() = runTest {
		val data = state<Int> {
			throw Status.StandardFailure(Status.StandardFailure.Kind.Invalid, "this is always invalid")
		}

		assertEquals(
			failed(Status.StandardFailure.Kind.Invalid, "this is always invalid"),
			data.firstResult()
		)
	}

	@Test
	fun throwOther() = runTest {
		val data = state<Int> {
			error("some error")
		}

		assertEquals(
			failed(
				Status.StandardFailure.Kind.Unknown,
				"Unknown error caught in the state builder"
			), data.firstResult()
		)
	}

}
