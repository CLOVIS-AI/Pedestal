package opensavvy.spine.ktor

import kotlinx.serialization.Serializable

@Serializable
data class NetworkResponse<T>(
	val routes: List<String>,
	val value: T,
)
