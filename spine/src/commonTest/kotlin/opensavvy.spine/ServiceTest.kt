@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("unused")

package opensavvy.spine

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import opensavvy.backbone.Backbone
import opensavvy.backbone.Ref
import opensavvy.backbone.Ref.Companion.request
import opensavvy.backbone.Ref.Companion.requestValue
import opensavvy.backbone.RefState
import opensavvy.backbone.defaultBackboneCache
import opensavvy.spine.Route.Companion.div
import opensavvy.state.*
import opensavvy.state.Slice.Companion.failed
import opensavvy.state.Slice.Companion.mapIdentifier
import opensavvy.state.Slice.Companion.successful
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
	inner class Departments : StaticResource<List<Id<Department>>, Department.SearchParams, Context>("departments") {
		inner class Unique : DynamicResource<Department, Context>("department") {
			inner class Users : StaticResource<List<Id<User>>, Parameters.Empty, Context>("users")

			val users = Users()
		}

		val id = Unique()
	}

	inner class Users : StaticResource<List<Id<User>>, Parameters.Empty, Context>("users") {
		inner class Unique : DynamicResource<User, Context>("user") {
			inner class Departments : StaticResource<List<Id<Department>>, Parameters.Empty, Context>("departments")

			val join = action<Unit, Unit, Parameters.Empty>(Route / "join")

			val leave = action<Unit, Unit, Parameters.Empty>(Route / "leave")

			val rename = edit<User.Rename, Parameters.Empty>(Route / "name") { id, newName, _, _ ->
				ensureValid(
					id.unit,
					newName.name.isNotBlank()
				) { "A user's name may not be empty: '${newName.name}'" }
			}

			val departments = Departments()
		}

		val create = create { id, it: User.New, _: Parameters.Empty, context: Context ->
			ensureValid(id.unit, it.name.isNotBlank()) { "A user's name may not be empty: '${it.name}'" }
			ensureValid(
				id.unit,
				it.name.length < 100
			) { "A user's name may not be longer than 100 characters, found ${it.name.length} characters: '${it.name}'" }
			ensureAuthorized(id = null, context.user.requestValue().admin) { "Only admins can create new users" }
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
		override val cache = defaultBackboneCache<User>()

		private val users = HashMap<String, User>()

		init {
			users["0"] = User("Employee", admin = false)
			users["1"] = User("Admin", admin = true)
		}

		override fun directRequest(ref: Ref<User>): RefState<User> = flow {
			ensureValid(ref, ref is Ref.Basic) { "The reference type ${ref::class} is not supported by UserBone" }

			val result = users[ref.id]
			ensureFound(ref, result != null) { "No user has the ID $ref" }

			emitSuccessful(ref, result)
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
			successful(id1, User("Employee", false)),
			state {
				endpoint.validate(this, id1, Unit, Parameters.Empty, employee)
				emitAll(employee.user.request().mapIdentifier { endpoint.idOf((it as Ref.Basic).id) })
			}.firstResult()
		)

		// Scenario 2: access with an invalid service ID

		val id2 = Id<User>("this-is-not-the-correct-service-name", Route / "users" / "0")
		assertEquals(
			failed(
				id2,
				Status.StandardFailure.Kind.Invalid,
				"The passed identifier refers to the service 'this-is-not-the-correct-service-name', but this resource belongs to the service 'v2'"
			),
			state {
				endpoint.validate(this, id2, Unit, Parameters.Empty, employee)
			}.firstResult()
		)

		// Scenario 3: access with an invalid ID (too short)

		val id3 = Id<User>("v2", Route / "users") // should be /users/0
		assertEquals(
			failed(
				id3,
				Status.StandardFailure.Kind.Invalid,
				"The passed identifier's URI length is too short for this resource: 'v2/users' for resource 'v2/users/{user}'"
			),
			state {
				endpoint.validate(this, id3, Unit, Parameters.Empty, employee)
			}.firstResult()
		)

		// Scenario 4: access with an invalid ID (wrong resource)

		val id4 = Id<User>("v2", Route / "departments" / "0") // should be /users/0
		assertEquals(
			failed(
				id4,
				Status.StandardFailure.Kind.Invalid,
				"The passed identifier's segment #0 doesn't match the resource; expected 'users' but found 'departments'"
			),
			state {
				endpoint.validate(this, id4, Unit, Parameters.Empty, employee)
			}.firstResult()
		)

		// Scenario 5: access with an invalid ID (too long)

		val id5 = Id<User>("v2", Route / "departments" / "users" / "0") // should be /users/0
		assertEquals(
			failed(
				id5,
				Status.StandardFailure.Kind.Invalid,
				"The passed identifier's URI length is too long for this resource: 'v2/departments/users/0' for resource 'v2/users/{user}'"
			),
			state {
				endpoint.validate(this, id5, Unit, Parameters.Empty, employee)
			}.firstResult()
		)
	}

	@Test
	fun createUser() = runTest {
		val api = Api()
		val endpoint = api.users.create
		val bone = UserBone()

		val admin = Context(Ref.Basic("1", bone))

		assertEquals(emptyList(), state {
			endpoint.validate(this, endpoint.idOf(), User.New("Third user"), Parameters.Empty, admin)
		}.skipLoading().toList())
	}

	@Test
	fun editUser() = runTest {
		val api = Api()
		val endpoint = api.users.id.rename
		val bone = UserBone()

		val admin = Context(Ref.Basic("1", bone))

		val id = endpoint.idOf("0")
		assertEquals(emptyList(), state {
			endpoint.validate(this, id, User.Rename("Another name"), Parameters.Empty, admin)
		}.skipLoading().toList())
	}

}
