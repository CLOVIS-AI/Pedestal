package opensavvy.spine.ktor

import kotlinx.serialization.Serializable
import opensavvy.spine.Id

@Serializable
data class NetworkResponse<T>(
	val id: Id<T>,
	val routes: List<String>,
	val value: T,
)
