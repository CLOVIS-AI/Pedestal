package opensavvy.spine

import kotlinx.serialization.Serializable
import opensavvy.state.Identifier

@Serializable
data class Id<T>(
	val service: Route.Segment,
	val resource: Route,
) : Identifier<T> {

	constructor(service: String, resource: Route) : this(Route.Segment(service), resource)

	/**
	 * Untyped [Id] with the same data as this instance.
	 *
	 * Operations that have no resulting data (i.e. only have side effects) are represented by returning [Unit].
	 * However, it is still useful to return an ID to mark which object caused an error.
	 * This property is used to obtain an [Id] instance useful in methods that return no result.
	 */
	val unit: Id<Unit>
		get() = Id(service, resource)

	override fun toString() = "$service/$resource"
}
