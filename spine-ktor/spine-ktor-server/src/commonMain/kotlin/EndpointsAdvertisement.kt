package opensavvy.spine.ktor.server

import io.ktor.server.application.*
import io.ktor.server.response.*
import opensavvy.spine.Id
import opensavvy.spine.Operation
import opensavvy.spine.Route
import opensavvy.spine.Route.Companion.div
import opensavvy.spine.ktor.toHttp

fun ApplicationCall.advertiseEndpointsFor(operation: Operation<*, *, *, *, *, *>, id: Id) {
	val resource = operation.resource

	val link = resource.operations
		.groupBy(
			keySelector = { it.route ?: Route.Root },
			valueTransform = { it.kind }
		)
		.map { (route, kinds) ->
			val attributes = buildList {
				if (route.segments.isEmpty())
					add("canonical")

				for (kind in kinds.toSet())
					add(kind.toHttp().value.lowercase())
			}
			"<${Route / id.service / id.resource / route}>; rel=\"${attributes.joinToString(separator = ",")}\""
		}
		.joinToString(separator = ", ")

	if (link.isNotBlank())
		response.header("Link", link)
}
