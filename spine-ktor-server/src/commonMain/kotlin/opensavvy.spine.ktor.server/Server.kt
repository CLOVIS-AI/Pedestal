package opensavvy.spine.ktor.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import opensavvy.backbone.Data.Companion.firstResult
import opensavvy.backbone.Data.Companion.state
import opensavvy.backbone.Result
import opensavvy.backbone.Result.Failure.Standard.Kind.*
import opensavvy.spine.Operation
import opensavvy.spine.Parameters

/**
 * Builds a route to match the specified [operation].
 *
 * This function converts the information declared in [Spine's operation][Operation] into a route declaration using
 * [Ktor's route][io.ktor.server.routing.route].
 *
 * For example, let's imagine an operation `api.users.get` which returns a list of users and takes no parameters.
 * ```kotlin
 * val context = ContextGenerator { /* see the ContextGenerator interface */ }
 *
 * routing {
 *     route(api.users.get, context) {
 *         // Access the request context and query parameters
 *         if (!context.admin && parameters.allowArchived)
 *             // Easily validate values
 *             markUnauthorized(…) // Automatically converted to HTTP 403 Forbidden
 *
 *         // Set additional Ktor operations not modeled by Spine
 *         call.setCookie(…)
 *
 *         // Use Pedestal State methods, not call.respond!
 *         markCompleted(…)
 *     }
 * }
 * ```
 * For more information about the data available in the `route` block, see [ResponseStateBuilder].
 *
 * This function automatically calls the [operation]'s [validation][Operation.validate] code.
 */
inline fun <Resource : Any, reified In : Any, reified Out : Any, reified Params : Parameters, Context : Any> Route.route(
	operation: Operation<Resource, In, Out, Params, Context>,
	contextGenerator: ContextGenerator<Context>,
	crossinline block: suspend ResponseStateBuilder<In, Out, Params, Context>.() -> Unit,
) {
	val path: String = operation.resource.routeTemplate + (operation.route ?: "")

	val method = when (operation.kind) {
		Operation.Kind.Read -> HttpMethod.Get
		Operation.Kind.Create -> HttpMethod.Post
		Operation.Kind.Edit -> HttpMethod.Patch
		Operation.Kind.Delete -> HttpMethod.Delete
	}

	route(path, method) {
		handle {
			val context = contextGenerator.generate(call)

			val params = Params::class.java
				.getConstructor()
				.newInstance() // Created via reflection, assumes the default constructor is present
			for ((name, values) in call.parameters.entries())
				params.data[name] = values.first() // if a parameter is added multiple times, only the first one is kept

			val body = call.receive<In>()

			val state = state {
				operation.validate(this, body, params, context)

				val responseBuilder = ResponseStateBuilder(this, body, params, call, context)
				responseBuilder.block()
			}.firstResult() // How can we send loading events via HTTP?

			when (val data = state.data) {
				is Result.Success -> call.respond(data.value)

				// This should not be possible (it only happens for loading events, and we ignored them)
				is Result.NoData -> error("The server did not find any value to return, this should not be possible")

				is Result.Failure.Standard -> when (data.kind) {
					Invalid -> call.respond(HttpStatusCode.UnprocessableEntity, data.message)
					Unauthenticated -> call.respond(HttpStatusCode.Unauthorized, data.message)
					Unauthorized -> call.respond(HttpStatusCode.Forbidden, data.message)
					NotFound -> call.respond(HttpStatusCode.NotFound, data.message)
					Unknown -> call.respond(HttpStatusCode.InternalServerError, data.message)
				}

				is Result.Failure.Kotlin -> call.respond(HttpStatusCode.InternalServerError, data.message)
			}
		}
	}
}
