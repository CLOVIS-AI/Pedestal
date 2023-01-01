package opensavvy.spine

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * An API route.
 *
 * This is a suffix that can be added to the API URI to create a valid URI to a route of the API.
 * To convert this class into a URI-compatible format, use [toString].
 *
 * To conveniently create instances of this class, two shorthands are provided:
 * ```kotlin
 * val first = Route / "test"
 * val second = first / "other"
 * ```
 */
@Serializable(with = Route.Serializer::class)
data class Route(val segments: List<Segment>) {

	/**
	 * A route segment.
	 *
	 * Segments may only be composed of characters explicitly unreserved in URIs.
	 */
	@Serializable(with = Segment.Serializer::class)
	data class Segment(val segment: String) {
		init {
			for (char in segment) {
				if (char == '&' || char == '/' || char == '?')
					throw IllegalArgumentException("A route segment cannot be composed of the characters '&', '/', or '?'; found character '$char' in segment '$segment'")
			}
		}

		override fun toString() = segment

		/**
		 * Serializer for [Segment].
		 *
		 * Without this serializer, segments would be serialized as objects: `{ "segment": "foo" }`.
		 * With this serializer, segments are serialized as values: `"foo"`.
		 */
		internal object Serializer : KSerializer<Segment> {
			override val descriptor = PrimitiveSerialDescriptor("opensavvy.spine.Route.Segment", PrimitiveKind.STRING)

			override fun serialize(encoder: Encoder, value: Segment) {
				encoder.encodeString(value.segment)
			}

			override fun deserialize(decoder: Decoder): Segment {
				return Segment(decoder.decodeString())
			}
		}
	}

	override fun toString() = segments.joinToString(separator = "/")

	/**
	 * Serializer for [Route].
	 *
	 * Without this serializer, routes would be serialized as objects of arrays: `{ segments: ["foo", "bar"] }`.
	 * With this serializer, routes are serialized as values: `"foo/bar"`.
	 */
	internal object Serializer : KSerializer<Route> {
		override val descriptor = PrimitiveSerialDescriptor("opensavvy.spine.Route", PrimitiveKind.STRING)

		override fun serialize(encoder: Encoder, value: Route) {
			encoder.encodeString(value.toString())
		}

		override fun deserialize(decoder: Decoder): Route {
			return Route(decoder.decodeString().split("/").map { Segment(it) })
		}
	}

	companion object {

		/**
		 * The empty [Route].
		 */
		val Root = Route(emptyList())

		/**
		 * Shorthand to create a sub-route named [id] from the current route.
		 */
		operator fun Route.div(id: String) = Route(segments + Segment(id))

		/**
		 * Shorthand to create a sub-route named [id] from the current route.
		 */
		operator fun Route.div(id: Segment) = Route(segments + id)

		/**
		 * Shorthand to concatenate [other] at the end of this route.
		 */
		operator fun Route.div(other: Route) = Route(segments + other.segments)

		/**
		 * Shorthand to create a top-level route named [id] (its parent is the [Root]).
		 */
		@Suppress("RemoveRedundantQualifierName") // could be declared on Companion, but I think it's easier to read this way
		operator fun Route.Companion.div(id: String) = Route(listOf(Segment(id)))

		/**
		 * Shorthand to create a top-level route named [id] (its parent is the [Root]).
		 */
		@Suppress("RemoveRedundantQualifierName") // could be declared on Companion, but I think it's easier to read this way
		operator fun Route.Companion.div(id: Segment) = Route(listOf(id))

	}
}
