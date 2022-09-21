package opensavvy.spine

import opensavvy.backbone.Data.Companion.markInvalid
import opensavvy.backbone.Data.Companion.markUnauthorized
import opensavvy.backbone.Ref
import opensavvy.backbone.Ref.Companion.requestValue
import opensavvy.spine.Route.Companion.div
import kotlin.test.Test
import kotlin.test.assertEquals

/*
 * Example API:
 * /departments                  List of IDs of departments
 * /departments?public=true      List of IDs of public departments
 * /departments/{id}             Info about a single department
 * /departments/{id}/users       Query the list of users in that department
 * /users                        List of IDs of users
 * /users/{id}                   Info about a single user
 * /users/{id}/departments       List of departments a user is a part of
 * /users/{id}/join              PATCH
 * /users/{id}/leave             PATCH
 */

private data class Department(val name: String)
private data class User(val name: String, val admin: Boolean) {

	data class New(val name: String)
}

private class Context(val user: Ref<User>)

private class Api : Service("v2") {
	inner class Departments : StaticResource<List<Id<Department>>, Context>("departments") {
		inner class Unique : DynamicResource<Department, Context>("department") {
			inner class Users : StaticResource<List<Id<User>>, Context>("users")

			val users = Users()
		}

		val id = Unique()
	}

	inner class Users : StaticResource<List<Id<User>>, Context>("users") {
		inner class Unique : DynamicResource<User, Context>("user") {
			inner class Departments : StaticResource<List<Id<Department>>, Context>("departments")

			val join = edit<Unit>(Route / "join")

			val leave = edit<Unit>(Route / "leave")

			val departments = Departments()
		}

		val create = create { it: User.New, context ->
			if (it.name.isBlank())
				markInvalid(ref = null, "A user's name may not be empty: '${it.name}'")

			if (it.name.length < 100)
				markInvalid(
					ref = null,
					"A user's name may not be longer than 100 characters, found ${it.name.length} characters"
				)

			if (!context.user.requestValue().admin)
				markUnauthorized(ref = null, "Only admins can create new users")
		}

		val id = Unique()
	}

	val departments = Departments()
	val users = Users()
}

class ServiceTest {

	@Test
	fun routeGeneration() {
		val api = Api()

		val routes = api.routesRecursively.joinToString(separator = "\n")

		val expected = """
			v2/departments
			v2/departments/{department}
			v2/departments/{department}/users
			v2/users
			v2/users/{user}
			v2/users/{user}/departments
		""".trimIndent()

		assertEquals(expected, routes)
	}

}
