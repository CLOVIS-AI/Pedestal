package opensavvy.spine.ktor

import io.ktor.http.*
import opensavvy.state.Status

fun HttpStatusCode.toSpine() = when (this) {
	HttpStatusCode.NotFound -> Status.StandardFailure.Kind.NotFound
	HttpStatusCode.Unauthorized -> Status.StandardFailure.Kind.Unauthenticated
	HttpStatusCode.Forbidden -> Status.StandardFailure.Kind.Unauthorized
	HttpStatusCode.UnprocessableEntity -> Status.StandardFailure.Kind.Invalid
	else -> Status.StandardFailure.Kind.Unknown
}

fun Status.StandardFailure.Kind.toHttp() = when (this) {
	Status.StandardFailure.Kind.Invalid -> HttpStatusCode.UnprocessableEntity
	Status.StandardFailure.Kind.Unauthenticated -> HttpStatusCode.Unauthorized
	Status.StandardFailure.Kind.Unauthorized -> HttpStatusCode.Forbidden
	Status.StandardFailure.Kind.NotFound -> HttpStatusCode.NotFound
	Status.StandardFailure.Kind.Unknown -> HttpStatusCode.InternalServerError
}
