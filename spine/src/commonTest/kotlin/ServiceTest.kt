@file:OptIn(ExperimentalCoroutinesApi::class)
@file:Suppress("unused")

package opensavvy.spine

import kotlinx.coroutines.ExperimentalCoroutinesApi
import opensavvy.backbone.Ref
import opensavvy.spine.Route.Companion.div

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

private class Context(val user: Ref<SpineFailure<Nothing>, User>)

private class Api : Service("v2") {
	inner class Departments : StaticResource<List<Id>, Nothing, Department.SearchParams, Context>("departments") {
		inner class Unique : DynamicResource<Department, Nothing, Context>("department") {
			inner class Users : StaticResource<List<Id>, Nothing, Parameters.Empty, Context>("users")

			val users = Users()
		}

		val id = Unique()
	}

	inner class Users : StaticResource<List<Id>, Nothing, Parameters.Empty, Context>("users") {
		inner class Unique : DynamicResource<User, Nothing, Context>("user") {
			inner class Departments : StaticResource<List<Id>, Nothing, Parameters.Empty, Context>("departments")

			val join = action<Unit, Nothing, Unit, Parameters.Empty>(Route / "join")

			val leave = action<Unit, Nothing, Unit, Parameters.Empty>(Route / "leave")

			val rename = edit<User.Rename, Nothing, Parameters.Empty>(Route / "name")

			val departments = Departments()
		}

		val create = create<User.New, Nothing, User, Parameters.Empty>()

		val id = Unique()
	}

	val departments = Departments()
	val users = Users()
}
