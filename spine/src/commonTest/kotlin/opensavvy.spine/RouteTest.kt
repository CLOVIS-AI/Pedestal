package opensavvy.spine

import opensavvy.spine.Route.Companion.div
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class RouteTest {

	@Test
	fun root() {
		assertEquals("", Route.Root.toString())
	}

	@Test
	fun firstElement() {
		assertEquals("test", (Route / "test").toString())
		assertEquals("test35-7_12~.", (Route / "test35-7_12~.").toString())

		assertFails { Route / "test with a space" }
		assertFails { Route / "test/with/slashes" }
	}

	@Test
	fun secondElement() {
		assertEquals("first/second", (Route / "first" / "second").toString())
	}

}
