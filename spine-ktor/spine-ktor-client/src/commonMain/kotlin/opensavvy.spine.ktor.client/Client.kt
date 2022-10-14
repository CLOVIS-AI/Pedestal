package opensavvy.spine.ktor.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import opensavvy.spine.Id
import opensavvy.spine.Operation
import opensavvy.spine.Parameters
import opensavvy.spine.ResourceGroup.AbstractResource
import opensavvy.spine.ktor.NetworkResponse
import opensavvy.spine.ktor.toHttp
import opensavvy.spine.ktor.toSpine
import opensavvy.state.Progression.Companion.done
import opensavvy.state.Slice.Companion.failed
import opensavvy.state.Slice.Companion.pending
import opensavvy.state.Slice.Companion.successful
import opensavvy.state.State
import opensavvy.state.state

/**
 * Executes a [HttpClient] request, with the information declared in an [Operation].
 *
 * This function converts the information declared in [Spine's Operation][Operation] into an actual HTTP request made
 * through Ktor's client implementation (see [Ktor's request][io.ktor.client.request.request]).
 * It is written to be as similar to use as Ktor's function.
 *
 * For example, let's imagine an operation `api.users.get` which returns a list of users and takes no parameters.
 * ```kotlin
 * val client = HttpClient { /* Ktor configuration */ }
 * client.request(api.users.get, api.users.get.idOf(), parameters, context)
 * ```
 *
 * This function will call the [operation]'s [validation][Operation.validate] code.
 *
 * @param operation The Spine endpoint that should be called.
 * @param id The identifier of the [Resource] object on which the endpoint is called.
 * It is usually generated using [AbstractResource.idOf].
 * @param input The body of the request, as declared in [operation].
 * @param parameters The query parameters accepted by the request, as declared in [operation].
 * @param context The context of the request, as declared in [operation].
 * @param contentType The MIME type used to encode [input]. JSON by default.
 * @param configuration Additional configuration passed to Ktor's `request` function.
 * This configuration is applied after the parameters from this request are applied, it is possible to override data set by this function.
 */
inline fun <Resource : Any, reified In : Any, reified Out : Any, reified Params : Parameters, Context : Any> HttpClient.request(
	operation: Operation<Resource, In, Out, Params, Context>,
	id: Id,
	input: In,
	parameters: Params,
	context: Context,
	contentType: ContentType = ContentType.Application.Json,
	crossinline configuration: HttpRequestBuilder.() -> Unit = {},
): State<Out> = state {
	emit(pending(0.0))

	operation.validate(this, id, input, parameters, context)

	emit(pending(0.1))

	val result = request {
		method = operation.kind.toHttp()

		url {
			// {baseUrl}/{service}/{path-to-resource}/{path-to-method}

			// /{service}
			appendPathSegments(id.service.segment)

			// /{path-to-resource}
			appendPathSegments(id.resource.segments.map { it.segment })

			// /{path-to-method}  (if present)
			operation.route?.let { route ->
				appendPathSegments(route.segments.map { it.segment })
			}
		}

		for ((name, value) in parameters.data)
			parameter(name, value)

		contentType(contentType)
		setBody(input)

		configuration()
	}

	emit(pending(0.9))

	if (result.status.isSuccess()) {
		val response: NetworkResponse<Out> = result.body()
		emit(successful(response.value))
	} else {
		val body = result.body<String>().ifBlank { "${result.status} with no provided body" }
		val kind = result.status.toSpine()
		emit(failed(kind, body, progression = done()))
	}
}
