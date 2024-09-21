package opensavvy.state

import com.benwoodworth.parameterize.parameterOf
import com.benwoodworth.parameterize.parameterize
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.progress.loading
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ProgressionTest : PreparedSpec({

	test("String representation of loading states") {
		assertEquals("Loading", loading().toString())
		assertEquals("Loading(0%)", loading(0.0).toString())
		assertEquals("Loading(33%)", loading(0.33).toString())
		assertEquals("Loading(100%)", loading(1.0).toString())
	}

	test("0% loading") {
		val start = loading(0.0)
		assertEquals(0.0, start.normalized)
		assertEquals(0, start.percent)
	}

	test("33% loading") {
		val third = loading(1.0 / 3)
		assertEquals(0.3333333333333333, third.normalized)
		assertEquals(33, third.percent)
	}

	test("100% loading") {
		val end = loading(1.0)
		assertEquals(1.0, end.normalized)
		assertEquals(100, end.percent)
	}

	suite("Illegal progression values") {
		parameterize {
			val parameter by parameterOf(-1.0, 1.01, 1.00000001, Double.MAX_VALUE, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY)

			test("A progress value of $parameter is not allowed") {
				assertFails {
					loading(parameter)
				}
			}
		}
	}
})
