@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.state

import arrow.core.right
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import opensavvy.progress.Progress
import opensavvy.progress.coroutines.StateFlowProgressReporter
import opensavvy.progress.coroutines.asCoroutineContext
import opensavvy.progress.coroutines.report
import opensavvy.progress.done
import opensavvy.progress.loading
import opensavvy.state.outcome.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class OutcomeTest {

	private data class IntId(val id: Int) {

		suspend fun request() = out {
			val id = this@IntId

			report(loading(0.0))
			delay(20)

			// Just imagine the conditions are related to what we're testing
			ensureValid(id.id > 0) { "The number should be greater than 0: ${id.id}" }
			report(loading(0.2))
			delay(20)

			ensureAuthenticated(id.id < 100) { "The number should be lesser than 100: ${id.id}" }
			report(loading(0.4))
			delay(20)

			ensureAuthorized(id.id % 2 == 0) { "The number should be even: ${id.id}" }
			report(loading(0.6))
			delay(20)

			ensureFound(id.id % 9 == 0) { "The number should be a multiple of 9: ${id.id}" }
			report(loading(0.8))
			delay(20)

			id.id
		}

		override fun toString() = "Id($id)"

	}

	@Test
	fun successful() = runTest {
		val id = IntId(18)
		val reporter = StateFlowProgressReporter()

		val actualAsync = async(reporter.asCoroutineContext()) {
			id.request().also { report(done()) }
		}

		val expectedLoading = listOf(
			loading(0.0),
			loading(0.2),
			loading(0.4),
			loading(0.6),
			loading(0.8),
		)

		val actualLoading = reporter.progress.takeWhile { it !is Progress.Done }.toList()

		val actual = actualAsync.await()
		assertEquals(18.right(), actual)
		assertEquals(18, actual.getOrNull())
		assertEquals(18, actual.orThrow())

		assertEquals(expectedLoading, actualLoading)
	}

	@Test
	fun invalid() = runTest {
		val id = IntId(-50)

		val actual = id
			.request()

		val expected = failed("The number should be greater than 0: -50", Failure.Kind.Invalid)

		assertEquals(expected, actual)
	}

	@Test
	fun unauthenticated() = runTest {
		val id = IntId(167)

		val actual = id.request()

		val expected = failed("The number should be lesser than 100: 167", Failure.Kind.Unauthenticated)

		assertEquals(expected, actual)
	}

	@Test
	fun unauthorized() = runTest {
		val id = IntId(5)

		val actual = id.request()

		val expected = failed("The number should be even: 5", Failure.Kind.Unauthorized)

		assertEquals(expected, actual)
	}

	@Test
	fun notFound() = runTest {
		val id = IntId(8)

		val actual = id.request()

		val expected = failed("The number should be a multiple of 9: 8", Failure.Kind.NotFound)

		assertEquals(expected, actual)
	}

	@Test
	fun identity() {
		val success0 = successful(0)
		val success1 = successful(1)
		assertEquals(successful(0), success0)
		assertEquals(successful(0).hashCode(), success0.hashCode())
		assertEquals(successful(1), success1)
		assertNotEquals(success0, success1)

		assertEquals("Either.Right(0)", success0.toString())
		assertEquals("Either.Right(1)", success1.toString())
		assertEquals("Loading", loading().toString())
		assertEquals("Loading(50%)", loading(0.5).toString())
		assertEquals("Loading(33%)", loading(0.33).toString())
	}

}
