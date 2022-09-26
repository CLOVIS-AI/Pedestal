@file:OptIn(ExperimentalCoroutinesApi::class)

package opensavvy.spine

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.test.runTest
import opensavvy.backbone.*
import opensavvy.backbone.Data.Companion.markCompleted
import opensavvy.backbone.Data.Companion.markInvalid
import opensavvy.backbone.Data.Companion.markNotFound
import opensavvy.backbone.Data.Companion.markUnauthorized
import opensavvy.backbone.Data.Companion.skipLoading
import opensavvy.backbone.Ref.Companion.request
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
			inner class Users : StaticResource<List<Id<User>>, Nothing?, Context>("users")

			val users = Users()
		}

		val id = Unique()
	}

	inner class Users : StaticResource<List<Id<User>>, Nothing?, Context>("users") {
		inner class Unique : DynamicResource<User, Context>("user") {
			inner class Departments : StaticResource<List<Id<Department>>, Nothing?, Context>("departments")

			val join = edit<Unit, Nothing?>(Route / "join")

			val leave = edit<Unit, Nothing?>(Route / "leave")

			val rename = edit<User.Rename, Nothing?>(Route / "name") { (_, newName), _, _ ->
				if (newName.name.isBlank())
					markInvalid(ref = null, "A user's name may not be empty: '${newName.name}'")
			}

			val departments = Departments()
		}

		val create = create { it: User.New, _: Nothing?, context: Context ->
			if (it.name.isBlank())
				markInvalid(ref = null, "A user's name may not be empty: '${it.name}'")

			if (it.name.length > 100)
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

	private class UserBone : Backbone<User> {
		override val cache: Cache<User> = Cache.Default()

		private val users = HashMap<String, User>()

		init {
			users["0"] = User("Employee", admin = false)
			users["1"] = User("Admin", admin = true)
		}

		override fun directRequest(ref: Ref<User>): Flow<Data<User>> = flow {
			if (ref !is Ref.Basic)
				markInvalid(ref, "The reference type ${ref::class} is not supported by UserBone")

			val result = users[ref.id] ?: markNotFound(ref, "No user has the ID $ref")

			markCompleted(ref, result)
		}
	}

	@Test
	fun getUser() = runTest {
		val api = Api()
		val endpoint = api.users.id.get
		val bone = UserBone()

		val employee = Context(Ref.Basic("0", bone))

		// Scenario 1: an employee accesses themselves

		val id1 = Id<User>("v2", Route / "users" / "0")
		assertEquals(listOf(Data(Result.Success(User("Employee", false)), Data.Status.Completed, employee.user)), flow {
			endpoint.validate(this, id1, null, employee)
			emitAll(employee.user.request())
		}.skipLoading().toList())

		// Scenario 2: access with an invalid service ID

		val id2 = Id<User>("this-is-not-the-correct-service-name", Route / "users" / "0")
		assertEquals(
			listOf(
				Data<User>(
					Result.Failure.Standard(
						Result.Failure.Standard.Kind.Invalid,
						"The passed identifier refers to the service 'this-is-not-the-correct-service-name', but this resource belongs to the service 'v2'"
					), Data.Status.Completed, null
				)
			), flow {
				endpoint.validate(this, id2, null, employee)
			}.skipLoading().catch { /* https://github.com/Kotlin/kotlinx.coroutines/issues/3463 */ }.toList()
		)

		// Scenario 3: access with an invalid ID (too short)

		val id3 = Id<User>("v2", Route / "users") // should be /users/0
		assertEquals(
			listOf(
				Data<User>(
					Result.Failure.Standard(
						Result.Failure.Standard.Kind.Invalid,
						"The passed identifier's URI length is too short for this resource: 'v2/users' for resource 'v2/users/{user}'"
					), Data.Status.Completed, null
				)
			), flow {
				endpoint.validate(this, id3, null, employee)
			}.skipLoading().catch { /* https://github.com/Kotlin/kotlinx.coroutines/issues/3463 */ }.toList()
		)

		// Scenario 4: access with an invalid ID (wrong resource)

		val id4 = Id<User>("v2", Route / "departments" / "0") // should be /users/0
		assertEquals(
			listOf(
				Data<User>(
					Result.Failure.Standard(
						Result.Failure.Standard.Kind.Invalid,
						"The passed identifier's segment #0 doesn't match the resource; expected 'users' but found 'departments'"
					), Data.Status.Completed, null
				)
			), flow {
				endpoint.validate(this, id4, null, employee)
			}.skipLoading().catch { /* https://github.com/Kotlin/kotlinx.coroutines/issues/3463 */ }.toList()
		)

		// Scenario 5: access with an invalid ID (too long)

		val id5 = Id<User>("v2", Route / "departments" / "users" / "0") // should be /users/0
		assertEquals(
			listOf(
				Data<User>(
					Result.Failure.Standard(
						Result.Failure.Standard.Kind.Invalid,
						"The passed identifier's URI length is too long for this resource: 'v2/departments/users/0' for resource 'v2/users/{user}'"
					), Data.Status.Completed, null
				)
			), flow {
				endpoint.validate(this, id5, null, employee)
			}.skipLoading().catch { /* https://github.com/Kotlin/kotlinx.coroutines/issues/3463 */ }.toList()
		)
	}

	@Test
	fun createUser() = runTest {
		val api = Api()
		val endpoint = api.users.create
		val bone = UserBone()

		val admin = Context(Ref.Basic("1", bone))

		assertEquals(emptyList(), flow {
			endpoint.validate(this, User.New("Third user"), null, admin)
		}.skipLoading().toList())
	}

	@Test
	fun editUser() = runTest {
		val api = Api()
		val endpoint = api.users.id.rename
		val bone = UserBone()

		val admin = Context(Ref.Basic("1", bone))

		val id = Id<User>("v2", Route / "users" / "0") // should be /users/0
		assertEquals(emptyList(), flow {
			endpoint.validate(this, id to User.Rename("Another name"), null, admin)
		}.skipLoading().toList())
	}

}
