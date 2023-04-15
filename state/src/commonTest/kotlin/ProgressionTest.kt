package opensavvy.state

import opensavvy.progress.loading
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class ProgressionTest {

	@Test
	fun string() {
		assertEquals("Loading", loading().toString())
		assertEquals("Loading(0%)", loading(0.0).toString())
		assertEquals("Loading(33%)", loading(0.33).toString())
		assertEquals("Loading(100%)", loading(1.0).toString())
	}

	@Test
	fun percent0() {
		val start = loading(0.0)
		assertEquals(0.0, start.normalized)
		assertEquals(0, start.percent)
	}

	@Test
	fun percentThird() {
		val third = loading(1.0 / 3)
		assertEquals(0.3333333333333333, third.normalized)
		assertEquals(33, third.percent)
	}

	@Test
	fun percent100() {
		val end = loading(1.0)
		assertEquals(1.0, end.normalized)
		assertEquals(100, end.percent)
	}

	@Test
	fun percentRange() {
		assertFails { loading(-1.0) }
		assertFails { loading(1.01) }
		assertFails { loading(1.00000001) }
		assertFails { loading(Double.MAX_VALUE) }
		assertFails { loading(Double.NEGATIVE_INFINITY) }
		assertFails { loading(Double.POSITIVE_INFINITY) }
	}
}
