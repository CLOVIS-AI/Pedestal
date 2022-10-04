package opensavvy.spine.ktor.server

import io.ktor.server.application.*

/**
 * Generates a request's context.
 *
 * Unlike the client implementation which accepts a context instance directly, the Ktor server must generate the context
 * on each request (because the context is tied to the user making the request).
 *
 * This interface is responsible for generating the context from an [ApplicationCall].
 */
fun interface ContextGenerator<Context> {
	suspend fun generate(call: ApplicationCall): Context
}
