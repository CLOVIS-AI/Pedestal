package opensavvy.spine.ktor.server

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import opensavvy.logger.Logger.Companion.warn
import opensavvy.logger.loggerFor
import opensavvy.spine.Operation
import opensavvy.spine.Parameters
import opensavvy.spine.ktor.NetworkResponse
import opensavvy.spine.ktor.toHttp
import opensavvy.state.Status
import opensavvy.state.firstResult
import opensavvy.state.state

object Server {
	val log = loggerFor(this)
}

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
	val path = buildString {
		append(operation.resource.routeTemplate)

		for (segment in operation.route?.segments ?: emptyList()) {
			append('/')
			append(segment.segment)
		}
	}

	val method = operation.kind.toHttp()
	route(path, method) {
		handle {
			val context = contextGenerator.generate(call)

			val id = call.generateId(operation.resource)

			val params: Params = when {
				Params::class == Parameters.Empty::class -> Parameters.Empty as Params
				else -> {
					val params = Params::class.java
						.getConstructor()
						.newInstance()
					for ((name, values) in call.parameters.entries())
					// if a parameter is added multiple times, only the first one is kept
						params.data[name] = values.first()
					params
				}
			}

			val body = when {
				// If the expected input is Unit, don't even try to read the body
				// Ktor fails to read the body on GET, DELETE and OPTIONS requests. Because we encode them as Unit,
				// it's not a problem.
				In::class == Unit::class -> Unit as In
				// For any other type, delegate to the ContentNegotiation plugin
				else -> call.receive()
			}

			val state = state {
				operation.validate(this, id, body, params, context)

				val responseBuilder = ResponseStateBuilder(this, id, body, params, call, context)
				responseBuilder.block()
			}.firstResult() // How can we send loading events via HTTP?

			when (val data = state.status) {
				is Status.Successful -> call.respond(
					NetworkResponse(
						routes = emptyList(), //TODO in #22: advertise the endpoints
						value = data.value
					)
				)

				// This should not be possible (it only happens for loading events, and we ignored them)
				is Status.Pending -> error("The server did not find any value to return, this should not be possible")

				is Status.StandardFailure -> {
					Server.log.warn(data.kind, data.message, data.cause?.stackTraceToString()) { "Failed request" }
					val code = data.kind.toHttp()
					call.respond(code, data.message ?: "No message")
				}

				is Status.Failed -> {
					Server.log.warn(data.message, data.stackTraceToString()) { "Failed request" }
					call.respond(HttpStatusCode.InternalServerError, data.message ?: "No message")
				}
			}
		}
	}
}