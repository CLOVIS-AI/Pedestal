package opensavvy.backbone

import kotlin.test.Test
import kotlin.test.assertEquals

class ResultTest {

	private val toString = { it: Int -> it.toString() }

	@Test
	fun mapNoData() {
		// NoData doesn't store anything, so mapping its content should do nothing
		assertEquals(Result.NoData, Result.NoData.map(toString))
		assertEquals("NoData", Result.NoData.toString())
	}

	@Test
	fun mapSuccess() {
		val input = Result.Success(5)
		val expected = Result.Success("5")

		assertEquals(expected, input.map(toString))
	}

	@Test
	fun mapStandardFailure() {
		// Failures do not store anything, so mapping their content should do nothing
		val input = Result.Failure.Standard(Result.Failure.Standard.Kind.NotFound, "Could not find element with identifier 2")

		assertEquals(input, input.map(toString))
	}

	@Test
	fun mapKotlinFailure() {
		// Failures do not store anything, so mapping their content should do nothing
		val input = Result.Failure.Kotlin(RuntimeException("Could not find element with identifier 2"))

		assertEquals(input, input.map(toString))
	}

}
