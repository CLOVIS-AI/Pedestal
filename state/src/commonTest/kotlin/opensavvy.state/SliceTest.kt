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

	private data class IntId(val id: Int) : Identifier {

		fun request() = state {
			val id = this@IntId

			emit(pending(0.0))
			delay(20)

			// Just imagine the conditions are related to what we're testing
			ensureValid(id.id > 0) { "The number should be greater than 0: ${id.id}" }
			emit(pending(0.2))

			ensureAuthenticated(id.id < 100) { "The number should be lesser than 100: ${id.id}" }
			emit(pending(0.4))

			ensureAuthorized(id.id % 2 == 0) { "The number should be even: ${id.id}" }
			emit(pending(0.6))

			ensureFound(id.id % 9 == 0) { "The number should be a multiple of 9: ${id.id}" }
			emit(pending(0.8))

			emit(successful(id.id))
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
			pending(0.0),
			pending(0.2),
			pending(0.4),
			pending(0.6),
			pending(0.8),
			successful(18),
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
			pending(0.0),
			failed(Status.StandardFailure.Kind.Invalid, "The number should be greater than 0: -50"),
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
			pending(0.0),
			pending(0.2),
			failed(Status.StandardFailure.Kind.Unauthenticated, "The number should be lesser than 100: 167"),
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
			pending(0.0),
			pending(0.2),
			pending(0.4),
			failed(Status.StandardFailure.Kind.Unauthorized, "The number should be even: 5"),
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
			pending(0.0),
			pending(0.2),
			pending(0.4),
			pending(0.6),
			failed(Status.StandardFailure.Kind.NotFound, "The number should be a multiple of 9: 8"),
		)

		assertEquals(expected, actual)
	}

	@Test
	fun destructuration() {
		val (status, progression) = successful(0)

		assertEquals(Status.Successful(0), status)
		assertEquals(Progression.done(), progression)
	}

	@Test
	fun identity() {
		val success0 = successful(0)
		val success1 = successful(1)
		assertEquals(successful(0), success0)
		assertEquals(successful(0).hashCode(), success0.hashCode())
		assertEquals(successful(1), success1)
		assertNotEquals(success0, success1)

		val pending0 = pending()
		val pending1 = pending(0.5)
		val pendingX = pending(1.0 / 3)
		assertEquals(pending(), pending0)
		assertEquals(pending(0.5), pending1)
		assertEquals(pending(0.5).hashCode(), pending1.hashCode())
		assertNotEquals(pending(0.6), pending1)
		assertEquals(pending(1.0 / 3), pendingX)

		val failed0 = failed(Status.StandardFailure.Kind.Invalid, "error")
		assertEquals(failed(Status.StandardFailure.Kind.Invalid, "error"), failed0)
		assertNotEquals(failed(Status.StandardFailure.Kind.NotFound, "error"), failed0)
		assertNotEquals(failed(Status.StandardFailure.Kind.Unauthenticated, "error"), failed0)
		assertNotEquals(failed(Status.StandardFailure.Kind.Invalid, "other"), failed0)

		assertEquals("0 Done", success0.toString())
		assertEquals("1 Done", success1.toString())
		assertEquals("Loading", pending0.toString())
		assertEquals("Loading(50%)", pending1.toString())
		assertEquals("Loading(33%)", pendingX.toString())
		assertEquals("Invalid(error) Done", failed0.toString())
	}

	@Test
	fun value() {
		val success = successful(0)
		val pending = pending()
		val failed1 = failed(IllegalArgumentException("whatever"), "whatever 2")
		val failed2 = failed(Status.StandardFailure.Kind.Invalid, "whatever 3")

		assertEquals(0, success.valueOrNull)

		assertEquals(null, pending.valueOrNull)
		assertEquals(null, failed1.valueOrNull)
		assertEquals(null, failed2.valueOrNull)

		assertFailsWith<NoSuchElementException> { pending.valueOrThrow }
		assertFailsWith<Status.ExceptionFailure>("whatever 2") { failed1.valueOrThrow }
		assertFailsWith<Status.StandardFailure>("whatever 3") { failed2.valueOrThrow }
	}

}
