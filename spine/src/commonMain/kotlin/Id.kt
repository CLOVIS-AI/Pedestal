package opensavvy.spine

import kotlinx.serialization.Serializable

@Serializable
data class Id(
	val service: Route.Segment,
	val resource: Route,
) {

	constructor(service: String, resource: Route) : this(Route.Segment(service), resource)

	override fun toString() = "$service/$resource"
}

@Serializable
data class Identified<T>(
	val id: Id,
	val value: T,
)
