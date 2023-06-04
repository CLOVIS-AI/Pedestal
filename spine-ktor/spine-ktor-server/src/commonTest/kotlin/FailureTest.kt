package opensavvy.spine.ktor.server

import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import opensavvy.spine.Parameters
import opensavvy.spine.Route
import opensavvy.spine.Route.Companion.div
import opensavvy.spine.Service
import opensavvy.spine.SpineFailure
import opensavvy.spine.ktor.client.request
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.success
import org.junit.Test
import org.slf4j.event.Level
import kotlin.test.assertEquals
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ClientContentNegotiation
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ServerContentNegotiation

class FailureTest {

	@Serializable
	private data class TestFailure(val a: Int)

	private class Api : Service("api") {
		inner class Resource : StaticResource<Unit, Unit, Parameters.Empty, Unit>("resource") {

			val success = action<Unit, TestFailure, Unit, Parameters.Empty>(Route / "success")
			val objectFailure = action<Unit, TestFailure, Unit, Parameters.Empty>(Route / "failWithObject")
			val textFailure = action<Unit, TestFailure, Unit, Parameters.Empty>(Route / "failWithText")

		}

		val resource = Resource()
	}

	@Test
	fun test() = testApplication {

		val api = Api()

		install(ServerContentNegotiation) {
			json()
		}

		install(CallLogging) {
			level = Level.DEBUG
		}

		routing {
			route(api.resource.success, { }) {
				// Nothing to do
			}

			route(api.resource.objectFailure, { }) {
				raise(SpineFailure(SpineFailure.Type.InvalidRequest, TestFailure(5)))
			}

			route(api.resource.textFailure, { }) {
				raise(SpineFailure(SpineFailure.Type.InvalidRequest, "5"))
			}
		}

		val client = createClient {
			install(ClientContentNegotiation) {
				json()
			}

			install(Logging) {
				level = LogLevel.ALL
			}
		}

		assertEquals(
			Unit.success(),
			client.request(api.resource.success, api.resource.idOf(), Unit, Parameters.Empty, Unit),
		)

		assertEquals(
			SpineFailure(SpineFailure.Type.InvalidRequest, TestFailure(5)).failed(),
			client.request(api.resource.objectFailure, api.resource.idOf(), Unit, Parameters.Empty, Unit),
		)

		assertEquals(
			SpineFailure(SpineFailure.Type.InvalidRequest, "5").failed(),
			client.request(api.resource.textFailure, api.resource.idOf(), Unit, Parameters.Empty, Unit),
		)
	}

}
