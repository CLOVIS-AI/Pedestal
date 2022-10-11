@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.state

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import opensavvy.state.Slice.Companion.failed
import opensavvy.state.Slice.Companion.pending
import opensavvy.state.Slice.Companion.successful
import opensavvy.state.Slice.Companion.valueOrNull
import opensavvy.state.Slice.Companion.valueOrThrow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class SliceTest {

	private data class IntId(val id: Int) : Identifier<Int> {

		fun request() = state {
			val id = this@IntId

			emitPending(id, 0.0)
			delay(20)

			// Just imagine the conditions are related to what we're testing
			ensureValid(id, id.id > 0) { "The number should be greater than 0: ${id.id}" }
			emitPending(id, 0.2)

			ensureAuthenticated(id, id.id < 100) { "The number should be lesser than 100: ${id.id}" }
			emitPending(id, 0.4)

			ensureAuthorized(id, id.id % 2 == 0) { "The number should be even: ${id.id}" }
			emitPending(id, 0.6)

			ensureFound(id, id.id % 9 == 0) { "The number should be a multiple of 9: ${id.id}" }
			emitPending(id, 0.8)

			emitSuccessful(id, id.id)
		}

		override fun toString() = "Id($id)"

	}

	@Test
	fun successful() = runTest {
		val id = IntId(18)

		val actual = id
			.request()
			.toList()

		val expected = listOf(
			pending(id, 0.0),
			pending(id, 0.2),
			pending(id, 0.4),
			pending(id, 0.6),
			pending(id, 0.8),
			successful(id, 18),
		)

		assertEquals(expected, actual)
		assertEquals(18, actual.last().valueOrThrow)
	}

	@Test
	fun invalid() = runTest {
		val id = IntId(-50)

		val actual = id
			.request()
			.toList()

		val expected = listOf(
			pending(id, 0.0),
			failed(id, Status.StandardFailure.Kind.Invalid, "The number should be greater than 0: -50"),
		)

		assertEquals(expected, actual)
	}

	@Test
	fun unauthenticated() = runTest {
		val id = IntId(167)

		val actual = id
			.request()
			.toList()

		val expected = listOf(
			pending(id, 0.0),
			pending(id, 0.2),
			failed(id, Status.StandardFailure.Kind.Unauthenticated, "The number should be lesser than 100: 167"),
		)

		assertEquals(expected, actual)
	}

	@Test
	fun unauthorized() = runTest {
		val id = IntId(5)

		val actual = id
			.request()
			.toList()

		val expected = listOf(
			pending(id, 0.0),
			pending(id, 0.2),
			pending(id, 0.4),
			failed(id, Status.StandardFailure.Kind.Unauthorized, "The number should be even: 5"),
		)

		assertEquals(expected, actual)
	}

	@Test
	fun notFound() = runTest {
		val id = IntId(8)

		val actual = id
			.request()
			.toList()

		val expected = listOf(
			pending(id, 0.0),
			pending(id, 0.2),
			pending(id, 0.4),
			pending(id, 0.6),
			failed(id, Status.StandardFailure.Kind.NotFound, "The number should be a multiple of 9: 8"),
		)

		assertEquals(expected, actual)
	}

	@Suppress("DestructuringWrongName") // false positive, see https://youtrack.jetbrains.com/issue/KT-54374
	@Test
	fun destructuration() {
		val (status, progression, id) = successful(IntId(0), 0)

		assertEquals(Status.Successful(0), status)
		assertEquals(Progression.done(), progression)
		assertEquals(IntId(0), id)
	}

	@Test
	fun identity() {
		val success0 = successful(IntId(0), 0)
		val success1 = successful(IntId(1), 1)
		assertEquals(successful(IntId(0), 0), success0)
		assertEquals(successful(IntId(0), 0).hashCode(), success0.hashCode())
		assertEquals(successful(IntId(1), 1), success1)
		assertNotEquals(success0, success1)

		val pending0 = pending(IntId(0))
		val pending1 = pending(IntId(1), 0.5)
		val pendingX = pending<IntId, Int>(id = null, 1.0 / 3)
		assertEquals(pending(IntId(0)), pending0)
		assertEquals(pending(IntId(1), 0.5), pending1)
		assertEquals(pending(IntId(1), 0.5).hashCode(), pending1.hashCode())
		assertNotEquals(pending(IntId(1), 0.6), pending1)
		assertEquals(pending(id = null, 1.0 / 3), pendingX)

		val failed0 = failed(IntId(0), Status.StandardFailure.Kind.Invalid, "error")
		assertEquals(failed(IntId(0), Status.StandardFailure.Kind.Invalid, "error"), failed0)
		assertNotEquals(failed(IntId(0), Status.StandardFailure.Kind.NotFound, "error"), failed0)
		assertNotEquals(failed(IntId(0), Status.StandardFailure.Kind.Unauthenticated, "error"), failed0)
		assertNotEquals(failed(IntId(0), Status.StandardFailure.Kind.Invalid, "other"), failed0)

		assertEquals("Id(0): 0 Done", success0.toString())
		assertEquals("Id(1): 1 Done", success1.toString())
		assertEquals("Id(0): Loading", pending0.toString())
		assertEquals("Id(1): Loading(50%)", pending1.toString())
		assertEquals("Loading(33%)", pendingX.toString())
		assertEquals("Id(0): Invalid(error) Done", failed0.toString())
	}

	@Test
	fun value() {
		val success = successful(IntId(0), 0)
		val pending = pending(IntId(0))
		val failed1 = failed(IntId(0), IllegalArgumentException("whatever"), "whatever 2")
		val failed2 = failed(IntId(0), Status.StandardFailure.Kind.Invalid, "whatever 3")

		assertEquals(0, success.valueOrNull)

		assertEquals(null, pending.valueOrNull)
		assertEquals(null, failed1.valueOrNull)
		assertEquals(null, failed2.valueOrNull)

		assertFailsWith<NoSuchElementException> { pending.valueOrThrow }
		assertFailsWith<Status.ExceptionFailure>("whatever 2") { failed1.valueOrThrow }
		assertFailsWith<Status.StandardFailure>("whatever 3") { failed2.valueOrThrow }
	}

}
