package opensavvy.spine

import kotlinx.serialization.Serializable

@Serializable
data class Id<@Suppress("unused") T>(
	val service: Route.Segment,
	val resource: Route,
)
