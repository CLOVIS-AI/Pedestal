package opensavvy.spine

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
class Route(val segments: List<Segment>) {

	/**
	 * A route segment.
	 *
	 * Segments may only be composed of characters explicitly unreserved in URIs.
	 */
	data class Segment(val segment: String) {
		init {
			for (char in segment) {
				if (!char.isLetterOrDigit() && char != '-' && char != '.' && char != '_' && char != '~')
					throw IllegalArgumentException("A route segment can only be composed of letter, digits, and the characters '-', '.', '_' and '~'; found character '$char' in segment '$segment'")
			}
		}

		override fun toString() = segment
	}

	override fun toString() = segments.joinToString(separator = "/")

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

	}
}
