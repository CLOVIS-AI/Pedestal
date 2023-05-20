package opensavvy.spine.ktor.server

import arrow.core.raise.either
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import opensavvy.logger.Logger.Companion.warn
import opensavvy.logger.loggerFor
import opensavvy.spine.Operation
import opensavvy.spine.Parameters
import opensavvy.spine.SpineFailure
import opensavvy.spine.ktor.toHttp
import opensavvy.state.arrow.toEither
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

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
inline fun <Resource : Any, reified In : Any, reified Failure : Any, reified Out : Any, reified Params : Parameters, Context : Any> Route.route(
	operation: Operation<Resource, In, Failure, Out, Params, Context>,
	contextGenerator: ContextGenerator<Context>,
	crossinline block: suspend ResponseStateBuilder<In, Failure, Params, Context>.() -> Out,
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

			call.advertiseEndpointsFor(operation, id)

			either<SpineFailure<Failure>, Out> {
				operation.validate(id, body, params, context).toEither().bind()

				val responseBuilder = ResponseStateBuilder(this, id, body, params, call, context)
				responseBuilder.block()
			}.fold(
				ifLeft = {
					Server.log.warn(it.type) { it.toString() }
					when (it) {
						is SpineFailure.Message -> call.respond(it.type.toHttp(), it.message ?: "No message")
						is SpineFailure.Payload -> call.respond(it.type.toHttp(), it.payload ?: "No message")
					}
				},
				ifRight = {
					call.respond(it)
				}
			)
		}
	}
}

// Yes, this is a copy-paste of the function above.
// For some reason, the compiler does not allow 'Nothing' as a reified type parameter, so I have to create an overload
// without that parameter. And because the entire function has to be inline, it has to be a copy.
// Spine is deprecated anyway, and will be completely rewritten when I have the time.
@JvmName("routeNoFailure")
inline fun <Resource : Any, reified In : Any, reified Out : Any, reified Params : Parameters, Context : Any> Route.route(
	operation: Operation<Resource, In, Nothing, Out, Params, Context>,
	contextGenerator: ContextGenerator<Context>,
	crossinline block: suspend ResponseStateBuilder<In, Nothing, Params, Context>.() -> Out,
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

			call.advertiseEndpointsFor(operation, id)

			either<SpineFailure<Nothing>, Out> {
				operation.validate(id, body, params, context).toEither().bind()

				val responseBuilder = ResponseStateBuilder(this, id, body, params, call, context)
				responseBuilder.block()
			}.fold(
				ifLeft = {
					Server.log.warn(it.type) { it.toString() }
					when (it) {
						is SpineFailure.Message -> call.respond(it.type.toHttp(), it.message ?: "No message")
						is SpineFailure.Payload -> call.respond(it.type.toHttp(), it.payload ?: "No message")
					}
				},
				ifRight = {
					call.respond(it)
				}
			)
		}
	}
}
