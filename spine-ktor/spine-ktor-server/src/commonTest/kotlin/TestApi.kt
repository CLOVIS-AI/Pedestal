package opensavvy.spine.ktor.server

import kotlinx.serialization.Serializable
import opensavvy.spine.Id
import opensavvy.spine.Parameters
import opensavvy.spine.Route
import opensavvy.spine.Route.Companion.div
import opensavvy.spine.Service

//region API objects declaration

@Serializable
data class User(val id: Id, val name: String, val archived: Boolean) {

	init {
		checkUsername(name)
	}

	companion object {
		private fun checkUsername(name: String) {
			for (char in name)
				require(!char.isWhitespace()) { "Usernames cannot contain whitespace: '$name'" }
		}
	}

	@Serializable
	data class New(val name: String) {
		init {
			checkUsername(name)
		}
	}

	class SearchParams : Parameters() {
		var includeArchived: Boolean by parameter("includeArchived")
	}

	sealed interface Failures {
		data class InvalidUsername(val username: String) : Failures
	}
}

//endregion
//region API endpoints declaration

class TestApi : Service("test") {

	inner class Users : StaticResource<List<Id>, Nothing, User.SearchParams, Unit>("users") {
		inner class Unique : DynamicResource<User, Nothing, Unit>("user") {

			val archive = action<Unit, Nothing, Unit, Parameters.Empty>(Route / "archive")

			val unarchive = action<Unit, Nothing, Unit, Parameters.Empty>(Route / "reopen")

			val delete = delete<Unit, Nothing>()
		}

		val create = create<User.New, User.Failures.InvalidUsername, User, Parameters.Empty>()

		val id = Unique()
	}

	val users = Users()
}

//endregion
