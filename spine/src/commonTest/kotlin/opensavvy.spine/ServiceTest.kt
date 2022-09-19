package opensavvy.spine

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
 * /users/{id}/departments/join  PUT
 * /users/{id}/departments/leave PUT
 */

private data class Department(val name: String)
private data class User(val name: String)

private class Api : Service("v2") {
	inner class Departments : StaticResource<List<Id<Department>>>("departments") {
		inner class Unique : DynamicResource<Department>("department") {
			inner class Users : StaticResource<List<Id<User>>>("users")

			val users = Users()
		}

		val id = Unique()
	}

	inner class Users : StaticResource<List<Id<User>>>("users") {
		inner class Unique : DynamicResource<User>("user") {
			inner class Departments : StaticResource<List<Id<Department>>>("departments")

			val departments = Departments()
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
