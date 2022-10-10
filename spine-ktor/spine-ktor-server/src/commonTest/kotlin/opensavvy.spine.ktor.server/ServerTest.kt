package opensavvy.spine.ktor.server

import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.testing.*
import kotlinx.coroutines.flow.onEach
import opensavvy.logger.Logger.Companion.debug
import opensavvy.logger.Logger.Companion.info
import opensavvy.logger.loggerFor
import opensavvy.spine.Id
import opensavvy.spine.Parameters
import opensavvy.spine.Route
import opensavvy.spine.Route.Companion.div
import opensavvy.spine.ktor.client.request
import opensavvy.state.emitSuccessful
import opensavvy.state.ensureFound
import opensavvy.state.firstResultOrThrow
import org.junit.Test
import org.slf4j.event.Level
import kotlin.test.assertEquals
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

class ServerTest {

	private val log = loggerFor(this).apply {
		level = opensavvy.logger.LogLevel.TRACE
	}

	@Test
	fun test() = testApplication {
		log.info { "Instantiating the API…" }
		val api = TestApi()

		//region Server-side implementation of the API
		log.info { "Configuring the server…" }

		install(ServerContentNegotiation) {
			json()
		}

		install(CallLogging) {
			level = Level.DEBUG
		}

		log.info { "Declaring the routes…" }
		val users = ArrayList<User>() // for the example, we're using a simple list and not a proper database
		var nextId = 0
		val context = ContextGenerator {}

		routing {
			trace { println(it) }

			route(api.users.get, context) {
				val result = users
					.filter { parameters.includeArchived || !it.archived }
					.map { it.id }
				emitSuccessful(api.users.get.idOf(), result)
			}

			route(api.users.create, context) {
				val name = body.name
				val newId = api.users.id.idOf((nextId++).toString())
				val new = User(newId, name, archived = false)
				users += new
				emitSuccessful(new.id, new)
			}

			route(api.users.id.get, context) {
				val user = users.find { it.id == id }
				ensureFound(id, user != null) { "Could not find the user $id" }
				emitSuccessful(id, user)
			}

			route(api.users.id.archive, context) {
				val userIndex = users.indexOfFirst { it.id == id }
				ensureFound(id, userIndex >= 0) { "Could not find user $id" }
				val user = users.removeAt(userIndex)
				users.add(user.copy(archived = true))
				emitSuccessful(id, user)
			}

			route(api.users.id.unarchive, context) {
				val userIndex = users.indexOfFirst { it.id == id }
				ensureFound(id, userIndex >= 0) { "Could not find user $id" }
				val user = users.removeAt(userIndex)
				users.add(user.copy(archived = false))
				emitSuccessful(id, user)
			}

			route(api.users.id.delete, context) {
				val userIndex = users.indexOfFirst { it.id == id }
				ensureFound(id, userIndex >= 0) { "Could not find user $id" }
				val user = users.removeAt(userIndex)
				emitSuccessful(id, user)
			}
		}

		//endregion
		//region Client-side usage of the API
		log.info { "Configuring the client…" }

		val client = createClient {
			install(ClientContentNegotiation) {
				json()
			}

			install(Logging) {
				level = LogLevel.ALL
			}
		}

		log.info { "Step 1: query the initial users (empty list)" }

		run {
			val params = User.SearchParams().apply { includeArchived = true }
			val results = client.request(api.users.get, api.users.get.idOf(), Unit, params, Unit)
				.onEach { log.debug(it) { "Received event for" } }
				.firstResultOrThrow()

			assertEquals(emptyList(), results)
		}

		log.info { "Step 2: creating two users" }

		run {
			val first =
				client.request(api.users.create, api.users.create.idOf(), User.New("first"), Parameters.Empty, Unit)
					.onEach { log.debug(it) { "Received event for" } }
					.firstResultOrThrow()

			val second =
				client.request(api.users.create, api.users.create.idOf(), User.New("second"), Parameters.Empty, Unit)
					.onEach { log.debug(it) { "Received event for" } }
					.firstResultOrThrow()

			assertEquals(User(Id("test", Route / "users" / "0"), "first", archived = false), first)
			assertEquals(User(Id("test", Route / "users" / "1"), "second", archived = false), second)

			val params = User.SearchParams().apply { includeArchived = true }
			val results = client.request(api.users.get, api.users.get.idOf(), Unit, params, Unit)
				.onEach { log.debug(it) { "Received event for" } }
				.firstResultOrThrow()

			assertEquals(listOf(first.id, second.id), results)
		}

		//endregion
	}
}
