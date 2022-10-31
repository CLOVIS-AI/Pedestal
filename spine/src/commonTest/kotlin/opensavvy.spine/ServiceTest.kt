@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("unused")

package opensavvy.spine

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import opensavvy.backbone.Backbone
import opensavvy.backbone.Ref
import opensavvy.backbone.Ref.Companion.requestValue
import opensavvy.backbone.defaultRefCache
import opensavvy.spine.Route.Companion.div
import opensavvy.state.*
import opensavvy.state.slice.*
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

private data class Department(val name: String) {
	class SearchParams : Parameters() {
		var showArchived by parameter("archived", default = false)
	}
}

private data class User(val name: String, val admin: Boolean) {

	data class New(val name: String)

	data class Rename(val name: String)
}

private class Context(val user: Ref<User>)

private class Api : Service("v2") {
	inner class Departments : StaticResource<List<Id>, Department.SearchParams, Context>("departments") {
		inner class Unique : DynamicResource<Department, Context>("department") {
			inner class Users : StaticResource<List<Id>, Parameters.Empty, Context>("users")

			val users = Users()
		}

		val id = Unique()
	}

	inner class Users : StaticResource<List<Id>, Parameters.Empty, Context>("users") {
		inner class Unique : DynamicResource<User, Context>("user") {
			inner class Departments : StaticResource<List<Id>, Parameters.Empty, Context>("departments")

			val join = action<Unit, Unit, Parameters.Empty>(Route / "join")

			val leave = action<Unit, Unit, Parameters.Empty>(Route / "leave")

			val rename = edit<User.Rename, Parameters.Empty>(Route / "name") {
				ensureValid(body.name.isNotBlank()) { "A user's name may not be empty: '${body.name}'" }
			}

			val departments = Departments()
		}

		val create = create<User.New, User, Parameters.Empty> {
			ensureValid(body.name.isNotBlank()) { "A user's name may not be empty: '${body.name}'" }
			ensureValid(body.name.length < 100) { "A user's name may not be longer than 100 characters, found ${body.name.length} characters: '${body.name}'" }
			ensureAuthorized(context.user.requestValue().bind().admin) { "Only admins can create new users" }
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

	private class UserBone : Backbone<User> {
		override val cache = defaultRefCache<User>()

		private val users = HashMap<String, User>()

		init {
			users["0"] = User("Employee", admin = false)
			users["1"] = User("Admin", admin = true)
		}

		override suspend fun directRequest(ref: Ref<User>) = slice {
			ensureValid(ref is Ref.Basic) { "The reference type ${ref::class} is not supported by UserBone" }

			val result = users[ref.id]
			ensureFound(result != null) { "No user has the ID $ref" }

			result
		}
	}

	@Test
	fun getUser() = runTest {
		val api = Api()
		val endpoint = api.users.id.get
		val bone = UserBone()

		val employee = Context(Ref.Basic("0", bone))

		// Scenario 1: an employee accesses themselves

		val id1 = endpoint.idOf("0")
		assertEquals(
			successful(User("Employee", false)),
			slice {
				endpoint.validate(id1, Unit, Parameters.Empty, employee).bind()
				employee.user.requestValue().bind()
			}
		)

		// Scenario 2: access with an invalid service ID

		val id2 = Id("this-is-not-the-correct-service-name", Route / "users" / "0")
		assertEquals(
			failed(
				"The passed identifier refers to the service 'this-is-not-the-correct-service-name', but this resource belongs to the service 'v2'",
				Failure.Kind.Invalid
			),
			endpoint.validate(id2, Unit, Parameters.Empty, employee)
		)

		// Scenario 3: access with an invalid ID (too short)

		val id3 = Id("v2", Route / "users") // should be /users/0
		assertEquals(
			failed(
				"The passed identifier's URI length is too short for this resource: 'v2/users' for resource 'v2/users/{user}'",
				Failure.Kind.Invalid
			),
			endpoint.validate(id3, Unit, Parameters.Empty, employee)
		)

		// Scenario 4: access with an invalid ID (wrong resource)

		val id4 = Id("v2", Route / "departments" / "0") // should be /users/0
		assertEquals(
			failed(
				"The passed identifier's segment #0 doesn't match the resource; expected 'users' but found 'departments'",
				Failure.Kind.Invalid
			),
			endpoint.validate(id4, Unit, Parameters.Empty, employee)
		)

		// Scenario 5: access with an invalid ID (too long)

		val id5 = Id("v2", Route / "departments" / "users" / "0") // should be /users/0
		assertEquals(
			failed(
				"The passed identifier's URI length is too long for this resource: 'v2/departments/users/0' for resource 'v2/users/{user}'",
				Failure.Kind.Invalid
			),
			endpoint.validate(id5, Unit, Parameters.Empty, employee)
		)
	}

	@Test
	fun createUser() = runTest {
		val api = Api()
		val endpoint = api.users.create
		val bone = UserBone()

		val admin = Context(Ref.Basic("1", bone))

		assertEquals(
			successful(Unit),
			endpoint.validate(endpoint.idOf(), User.New("Third user"), Parameters.Empty, admin)
		)
	}

	@Test
	fun editUser() = runTest {
		val api = Api()
		val endpoint = api.users.id.rename
		val bone = UserBone()

		val admin = Context(Ref.Basic("1", bone))

		val id = endpoint.idOf("0")
		assertEquals(
			successful(Unit),
			endpoint.validate(id, User.Rename("Another name"), Parameters.Empty, admin)
		)
	}

}
