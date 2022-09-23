package opensavvy.spine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue

class ParametersTest {

	@Test
	fun mandatory() {
		class MandatoryParams : Parameters() {
			var archived: Boolean by parameter("archived")
			var private: Boolean by parameter("private")
		}

		val params = MandatoryParams().apply {
			archived = true
		}

		assertTrue(params.archived)
		assertFails { params.private }

		assertEquals(
			mapOf(
				"archived" to "true"
			), params.data
		)
	}

	@Test
	fun optional() {
		class OptionalParams : Parameters() {
			var archived: Boolean? by parameter("archived")
			var private: Boolean? by parameter("private")
		}

		val params = OptionalParams().apply {
			archived = true
		}

		assertEquals(true, params.archived)
		assertEquals(null, params.private)

		assertEquals(
			mapOf(
				"archived" to "true"
			), params.data
		)
	}

	@Test
	fun default() {
		class DefaultValueParams : Parameters() {
			var archived: Boolean by parameter("archived", false)
			var private: Boolean by parameter("private", false)
		}

		val params = DefaultValueParams().apply {
			archived = true
		}

		assertEquals(true, params.archived)
		assertEquals(false, params.private)

		assertEquals(
			mapOf(
				"archived" to "true"
			), params.data
		)
	}

	@Test
	fun types() {
		class Types : Parameters() {
			var string: String by parameter("string")
			var bool: Boolean by parameter("bool")

			var byte: Byte by parameter("byte")
			var short: Short by parameter("short")
			var int: Int by parameter("int")
			var long: Long by parameter("long")

			var ubyte: UByte by parameter("ubyte")
			var ushort: UShort by parameter("ushort")
			var uint: UInt by parameter("uint")
			var ulong: ULong by parameter("ulong")

			var float: Float by parameter("float")
			var double: Double by parameter("double")
		}

		val params = Types().apply {
			string = "thing"
			bool = true

			byte = 1
			short = 2
			int = 3
			long = 4

			ubyte = 5u
			ushort = 6u
			uint = 7u
			ulong = 8u

			float = 9f
			double = 10.0
		}

		assertEquals("thing", params.string)
		assertEquals(true, params.bool)

		assertEquals(1, params.byte)
		assertEquals(2, params.short)
		assertEquals(3, params.int)
		assertEquals(4, params.long)

		assertEquals(5u, params.ubyte)
		assertEquals(6u, params.ushort)
		assertEquals(7u, params.uint)
		assertEquals(8u, params.ulong)

		assertEquals(9f, params.float)
		assertEquals(10.0, params.double)

		assertEquals(
			mapOf(
				"string" to "thing",
				"bool" to "true",

				"byte" to "1",
				"short" to "2",
				"int" to "3",
				"long" to "4",

				"ubyte" to "5",
				"ushort" to "6",
				"uint" to "7",
				"ulong" to "8",

				"float" to "9.0",
				"double" to "10.0",
			), params.data
		)
	}
}
