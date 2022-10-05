@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import opensavvy.state.Slice.Companion.failed
import opensavvy.state.Slice.Companion.successful
import kotlin.test.Test
import kotlin.test.assertEquals

class StateBuilderTest {

	private data class IntId(val id: Int) : Identifier<Int>

	@Test
	fun success() = runTest {
		val data = state {
			emitSuccessful(IntId(0), 0)
		}

		assertEquals(successful(IntId(0), 0), data.firstResult())
	}

	@Test
	fun ensure() = runTest {
		val data = state {
			ensureValid(IntId(0), condition = false) { "this is always invalid" }
		}

		assertEquals(
			failed(IntId(0), Status.StandardFailure.Kind.Invalid, "this is always invalid"),
			data.firstResult()
		)
	}

	@Test
	fun throwStandard() = runTest {
		val data = state {
			throw Status.StandardFailure(Status.StandardFailure.Kind.Invalid, "this is always invalid")
			@Suppress("UNREACHABLE_CODE") emitSuccessful(IntId(0), 0)
		}

		assertEquals(
			failed(id = null, Status.StandardFailure.Kind.Invalid, "this is always invalid"),
			data.firstResult()
		)
	}

	@Test
	fun throwOther() = runTest {
		val data = state {
			error("some error")
			@Suppress("UNREACHABLE_CODE") emitSuccessful(IntId(0), 0)
		}

		assertEquals(
			failed(
				id = null,
				Status.StandardFailure.Kind.Unknown,
				"Unknown error caught in the state builder"
			), data.firstResult()
		)
	}

}
