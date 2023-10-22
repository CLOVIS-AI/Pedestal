package opensavvy.spine

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import opensavvy.spine.Route.Companion.div
import kotlin.test.Test
import kotlin.test.assertEquals

class IdTest {

	@Test
	fun test() {
		val id = Id(service = "v2", resource = Route / "int" / "35eb")

		assertEquals("v2/int/35eb", id.toString())
	}

	@Test
	fun serialize() {
		val id = Id(service = "v2", resource = Route / "int" / "35eb")

		val serialized = Json.encodeToString(id)
		assertEquals("v2/int/35eb", id.toString())

		val deserialized = Json.decodeFromString<Id>(serialized)
		assertEquals(id, deserialized)
	}
}
