package opensavvy.spine.ktor.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import opensavvy.progress.coroutines.mapProgressTo
import opensavvy.spine.Id
import opensavvy.spine.Operation
import opensavvy.spine.Parameters
import opensavvy.spine.ResourceGroup.AbstractResource
import opensavvy.spine.SpineFailure
import opensavvy.spine.ktor.toHttp
import opensavvy.spine.ktor.toSpine
import opensavvy.state.arrow.out
import kotlin.js.JsName
import kotlin.jvm.JvmName

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
suspend inline fun <Resource : Any, reified In : Any, reified Failure : Any, reified Out : Any, reified Params : Parameters, Context : Any> HttpClient.request(
	operation: Operation<Resource, In, Failure, Out, Params, Context>,
	id: Id,
	input: In,
	parameters: Params,
	context: Context,
	contentType: ContentType = ContentType.Application.Json,
	crossinline onResponse: (HttpResponse) -> Unit = {},
	crossinline configuration: HttpRequestBuilder.() -> Unit = {},
) = out {
	mapProgressTo(0.0..0.1) {
		operation.validate(id, input, parameters, context).bind()
	}

	val result = mapProgressTo(0.1..0.9) {
		request {
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
	}

	mapProgressTo(0.9..0.95) {
		onResponse(result)
	}

	mapProgressTo(0.95..1.0) {
		if (result.status.isSuccess()) {
			result.body<Out>()
		} else {
			val kind = result.status.toSpine()

			val failure = try {
				SpineFailure(kind, result.body<Failure>())
			} catch (e: NoTransformationFoundException) {
				SpineFailure(kind, result.body<String>().ifBlank { "${result.status} with no provided body" })
			}

			raise(failure)
		}
	}
}

// Yes, this is a copy-paste of the function above.
// For some reason, the compiler does not allow 'Nothing' as a reified type parameter, so I have to create an overload
// without that parameter. And because the entire function has to be inline, it has to be a copy.
// Spine is deprecated anyway, and will be completely rewritten when I have the time.
@JvmName("requestNoFailure")
@JsName("requestNoFailure")
suspend inline fun <Resource : Any, reified In : Any, reified Out : Any, reified Params : Parameters, Context : Any> HttpClient.request(
	operation: Operation<Resource, In, Nothing, Out, Params, Context>,
	id: Id,
	input: In,
	parameters: Params,
	context: Context,
	contentType: ContentType = ContentType.Application.Json,
	crossinline onResponse: (HttpResponse) -> Unit = {},
	crossinline configuration: HttpRequestBuilder.() -> Unit = {},
) = out {
	mapProgressTo(0.0..0.1) {
		operation.validate(id, input, parameters, context).bind()
	}

	val result = mapProgressTo(0.1..0.9) {
		request {
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
	}

	mapProgressTo(0.9..0.95) {
		onResponse(result)
	}

	mapProgressTo(0.95..1.0) {
		if (result.status.isSuccess()) {
			result.body<Out>()
		} else {
			val kind = result.status.toSpine()

			raise(SpineFailure(kind, result.body<String>().ifBlank { "${result.status} with no provided body" }))
		}
	}
}
