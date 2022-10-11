package opensavvy.spine.ktor.server

import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.testing.*
import kotlinx.coroutines.flow.onEach
import opensavvy.backbone.Data.Companion.firstSuccessOrThrow
import opensavvy.backbone.Data.Companion.markCompleted
import opensavvy.backbone.Data.Companion.markNotFound
import opensavvy.logger.Logger.Companion.debug
import opensavvy.logger.Logger.Companion.info
import opensavvy.logger.loggerFor
import opensavvy.spine.ktor.client.request
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
		val context = ContextGenerator {}

		routing {
			route(api.users.get, context) {
				val result = users
					.filter { parameters.includeArchived || !it.archived }
					.map { it.id }
				markCompleted(ref = null, result)
			}

			route(api.users.create, context) {
				val name = body.name
				val newId = api.users.id.idOf(kotlin.random.Random.nextInt().toString())
				val new = User(newId, name, archived = false)
				markCompleted(ref = null, new)
			}

			route(api.users.id.get, context) {
				val id = body
				val user = users.find { it.id == id }
					?: markNotFound(ref = null, "Could not find the user $id")
				markCompleted(ref = null, user)
			}

			route(api.users.id.archive, context) {
				val (id, _) = body
				val userIndex = users.indexOfFirst { it.id == id }
				if (userIndex < 0)
					markNotFound(ref = null, "Could not find the user $id")
				val user = users.removeAt(userIndex)
				users.add(user.copy(archived = true))
				markCompleted(ref = null, user)
			}

			route(api.users.id.unarchive, context) {
				val (id, _) = body
				val userIndex = users.indexOfFirst { it.id == id }
				if (userIndex < 0)
					markNotFound(ref = null, "Could not find the user $id")
				val user = users.removeAt(userIndex)
				users.add(user.copy(archived = false))
				markCompleted(ref = null, user)
			}

			route(api.users.id.delete, context) {
				val (id, _) = body
				val userIndex = users.indexOfFirst { it.id == id }
				if (userIndex < 0)
					markNotFound(ref = null, "Could not find the user $id")
				val user = users.removeAt(userIndex)
				markCompleted(ref = null, user)
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

		val params = User.SearchParams().apply { includeArchived = true }
		val results = client.request(api.users.get, api.users.get.idOf(), api.users.get.idOf(), params, Unit)
			.onEach { log.debug(it) { "Received event for" } }
			.firstSuccessOrThrow()

		assertEquals(emptyList(), results)

		//endregion
	}
}
