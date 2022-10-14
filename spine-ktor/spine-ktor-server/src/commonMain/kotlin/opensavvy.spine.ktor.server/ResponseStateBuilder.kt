package opensavvy.spine.ktor.server

import io.ktor.server.application.*
import opensavvy.spine.Id
import opensavvy.spine.Parameters
import opensavvy.state.StateBuilder

/**
 * Information available in [route].
 */
class ResponseStateBuilder<In, Out, Params : Parameters?, Context>(
	builder: StateBuilder<Out>,

	/**
	 * The identifier of the resource being requested.
	 */
	val id: Id,

	/**
	 * The body of the request.
	 */
	val body: In,

	/**
	 * The query parameters of the request.
	 */
	val parameters: Params,

	/**
	 * Ktor's [ApplicationCall] instance, used to set parameters that Spine doesn't recognize (e.g. cookies).
	 */
	val call: ApplicationCall,

	/**
	 * The current request's context.
	 */
	val context: Context,
) : StateBuilder<Out> by builder
