package opensavvy.spine

import kotlinx.serialization.Serializable
import opensavvy.state.Identifier

@Serializable
data class Id(
	val service: Route.Segment,
	val resource: Route,
) : Identifier {

	constructor(service: String, resource: Route) : this(Route.Segment(service), resource)

	override fun toString() = "$service/$resource"
}
