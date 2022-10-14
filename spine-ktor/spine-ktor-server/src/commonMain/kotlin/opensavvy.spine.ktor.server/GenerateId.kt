package opensavvy.spine.ktor.server

import io.ktor.server.application.*
import opensavvy.spine.Id
import opensavvy.spine.ResourceGroup
import opensavvy.spine.Route

fun <O : Any> ApplicationCall.generateId(resource: ResourceGroup.AbstractResource<O, *>): Id {
	val values = ArrayList<String>()

	var cursor: ResourceGroup = resource
	while (cursor is ResourceGroup.AbstractResource<*, *>) {
		values += when (cursor) {
			is ResourceGroup.StaticResource<*, *, *> -> {
				cursor.route.segment
			}

			is ResourceGroup.DynamicResource<*, *> -> {
				val name = cursor.name
				val value = parameters[name]
					?: error("Missing path parameter: '{$name}' in '${resource.routeTemplate}'")
				value
			}
		}
		cursor = cursor.parent
	}

	values.reverse()

	val service = resource.service.name
	val route = Route(values.map { Route.Segment(it) })
	return Id(service, route)
}
