package opensavvy.spine.ktor

import io.ktor.http.*
import opensavvy.state.Failure

fun HttpStatusCode.toSpine() = when (this) {
	HttpStatusCode.NotFound -> Failure.Kind.NotFound
	HttpStatusCode.Unauthorized -> Failure.Kind.Unauthenticated
	HttpStatusCode.Forbidden -> Failure.Kind.Unauthorized
	HttpStatusCode.UnprocessableEntity -> Failure.Kind.Invalid
	else -> Failure.Kind.Unknown
}

fun Failure.Kind.toHttp() = when (this) {
	Failure.Kind.Invalid -> HttpStatusCode.UnprocessableEntity
	Failure.Kind.Unauthenticated -> HttpStatusCode.Unauthorized
	Failure.Kind.Unauthorized -> HttpStatusCode.Forbidden
	Failure.Kind.NotFound -> HttpStatusCode.NotFound
	Failure.Kind.Unknown -> HttpStatusCode.InternalServerError
}
