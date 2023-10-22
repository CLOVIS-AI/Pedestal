package opensavvy.spine.ktor

import io.ktor.http.*
import opensavvy.spine.SpineFailure

fun HttpStatusCode.toSpine() = when (this) {
	HttpStatusCode.NotFound -> SpineFailure.Type.NotFound
	HttpStatusCode.Unauthorized -> SpineFailure.Type.Unauthenticated
	HttpStatusCode.Forbidden -> SpineFailure.Type.Unauthorized
	HttpStatusCode.UnprocessableEntity -> SpineFailure.Type.InvalidRequest
	HttpStatusCode.Conflict -> SpineFailure.Type.InvalidState
	else -> error("Unexpected error code: $this")
}

fun SpineFailure.Type.toHttp() = when (this) {
	SpineFailure.Type.InvalidRequest -> HttpStatusCode.UnprocessableEntity
	SpineFailure.Type.Unauthenticated -> HttpStatusCode.Unauthorized
	SpineFailure.Type.Unauthorized -> HttpStatusCode.Forbidden
	SpineFailure.Type.NotFound -> HttpStatusCode.NotFound
	SpineFailure.Type.InvalidState -> HttpStatusCode.InternalServerError
}
