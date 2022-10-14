package opensavvy.spine

import opensavvy.spine.Route.Companion.div
import kotlin.test.Test
import kotlin.test.assertEquals

class IdTest {

	@Test
	fun test() {
		val id = Id(service = "v2", resource = Route / "int" / "35eb")

		assertEquals("v2/int/35eb", id.toString())
	}
}
