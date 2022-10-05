package opensavvy.spine.ktor

import io.ktor.http.*
import opensavvy.spine.Operation
import opensavvy.spine.Operation.Kind.*

fun Operation.Kind.toHttp() = when (this) {
	Read -> HttpMethod.Get
	Create -> HttpMethod.Post
	Edit -> HttpMethod.Patch
	Delete -> HttpMethod.Delete
}
