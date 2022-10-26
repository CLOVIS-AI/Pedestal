package opensavvy.spine

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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

		assertFails { Route / "test/with/slashes" }
	}

	@Test
	fun secondElement() {
		assertEquals("first/second", (Route / "first" / "second").toString())
	}

	@Test
	fun serializeSegment() {
		val segment = Route.Segment("whatever-hello")

		val segmentJson = Json.encodeToString(segment)
		assertEquals("\"whatever-hello\"", segmentJson)

		assertEquals(Route.Segment("whatever-hello"), Json.decodeFromString(segmentJson))
	}

	@Test
	fun serializeRoute() {
		val route = Route / "whatever" / "hello"

		val routeJson = Json.encodeToString(route)
		assertEquals("\"whatever/hello\"", routeJson)

		assertEquals(Route / "whatever" / "hello", Json.decodeFromString(routeJson))
	}

}
