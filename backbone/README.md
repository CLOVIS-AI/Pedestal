# Module backbone

The core of modern applications is often sending data between machines.
However, most applications do this inefficiently:

- data is sent recursively, sending objects the client likely already knows,
- caching is near impossible, because caching is not taken into account at the design stage,
- as a result, it is necessary to adopt a pessimistic approach to development, where each function will typically query the newest value of an object, modify edit, and push it the persistence layer.
- integration with reactive UI frameworks is also difficult: an object has no way to tell the framework when a new value is available.

Pedestal Backbone is an opinionated way of building APIs that attempts to solve these problems.
It builds upon Pedestal State and Pedestal Cache to integrate reactive caching in all levels of the API.

## Concept

The main concept is to separate all domain objects into three objects:

- The domain object itself, which should be immutable (e.g. a regular `data class` or `value class`),
- A reference provides a typesafe way to address the object throughout its lifetime and mutations. Accesses to the object through the reference are cached to avoid unnecessary request (implements the [Ref][opensavvy.backbone.Ref] interface),
- A Backbone provides methods operating on references to communicate with the external resource (implements the [Backbone][opensavvy.backbone.Backbone] interface). Traditionally, this would be a service or a DAO/Repository. Backbone is not tainted by any role, allowing it to be used in any layer of the application.

This architecture creates powerful patterns for application design, especially when coupled with reactive frameworks (such as React or Compose) or with the Kotlin Multiplatform technology.

### 1. Common module

As an example, let's explore a simple API consisting of a single object `Score` that the user may increase.

In our common module, we can define its API simply as:

```kotlin
import opensavvy.backbone.Backbone
import opensavvy.backbone.Ref as BackboneRef

// First, we create our score object as a regular data class
// The only requirement is that it should be immutable.
data class Score(
	val value: Int,
) {

	// We can now declare our service.
	// I personally prefer declaring the service as a nested interface 
	// (so it as addressed as Score.Service) instead of its own top-level 
	// interface (e.g. ScoreService), but this is entirely up to you.

	// The service implements the Backbone interface, which adds a 
	// 'directRequest' method to dereference a value, as well as the 
	// cache management.
	interface Service : Backbone {

		// Methods should:
		// - only accept/return references, and not actual values
		//   (this is necessary to ensure the cache catches all requests,
		//   the 'directRequest' method should be the only one which returns
		//   a real object).
		// - return an Outcome or ProgressiveOutcome instance for error management
		//   (see the documentation of Pedestal State).
		suspend fun increment(score: Ref, amount: Int = 1): Outcome<Unit>

		// Using the same rules, we see that search operations
		// return references instead of returning the value directly.
		suspend fun listMine(): Outcome<List<Ref>>
	}

	// We can now declare references to a specific score.
	// Mutability is expressed as a different value being returned upon
	// dereference operations over time.
	// Again, it is not mandatory to place the reference as a nested class.

	// The reference should store enough information for the service
	// implementations to find which object is referenced.
	// This could mean anything you want.

	// The reference doesn't necessarily have to be in the common module.
	// For example, you may want to have a different reference for the HTTP API
	// and the Repository layer.
	data class Ref(
		// For this example, the ID is a simple int, but it could be any type.
		// It could even be multiple fields, Backbone places no restrictions
		// on the way values are identified…
		val id: Int,
		// …except that each reference should know which Backbone
		// implementation is responsible for the operations executed on it.
		override val backbone: Service,
	) : BackboneRef {

		// Because we will always use references in our entire application,
		// it can be convenient to expose shorthands to execute operations.
		suspend fun increment(amount: Int = 1) = backbone.increment(this, amount)
	}
}
```

All that is left to do is to implement the `Score.Service` interface in our various modules (HTTP API, repository layer…).
Notice that the amount of code written is very similar to the amount of code necessary for a traditional approach, however,
as we will see in the next sections, this pattern is much more powerful.

### 2. Implementations and testing

Here is a sample client-side implementation of the interface, using a Ktor-inspired HTTP client:

```kotlin
import kotlin.coroutines.CoroutineContext

class ClientScores(
	private val client: HttpClient,
	coroutineContext: CoroutineContext,
) : Score.Service {

	// First, we must select our caching strategy.
	// For more information, see the Pedestal Cache documentation.
	// Here, we cache the values in RAM for a maximum of 15 minutes
	// (after which the values are either re-requested if they are
	// still needed, or forgotten otherwise).
	override val cache: RefCache<Score> = defaultRefCache()
		.cachedInMemory(coroutineContext)
		.expireAfter(15.minutes, coroutineContext)

	override suspend fun increment(score: Score.Ref, amount: Int) = out {
		// This is an imaginary HTTP client
		// Of course, Pedestal does not care what you use to make your request.
		// This could be anything you like.
		client.post("http://localhost:8080/${score.id}?amount=$amount")

		// We know the score was just modified, we thus clean the cache.
		// It will decide by itself whether it's better to re-query the value
		// or just to delete it.
		// It will also automatically notify all UI components.
		cache.expire(score)

		// If we were in a situation where the server returned the updated
		// value, we could instead inform the cache directly:
		// cache.update(score, newValue)

		// Notice that this function did NOT need to know what the current
		// value of the score is. In a traditional application where values
		// are passed to functions instead of references, a previous READ
		// operation would have been necessary to call this function.
	}

	override suspend fun listMine() = out {
		client.get<List<Int>>("http://localhost:8080/myScores")
			// convert the IDs to references linked to this 
			// backbone implementation
			.map { Score.Ref(it, this@ClientScores) }

		// Here, no value is modified, so we do not need to inform the 
		// cache of anything.

		// If this endpoint returned full values instead of just their IDs,
		// we could update them in the cache to avoid future dereference
		// requests.
	}

	// As mentioned previously, we must implement a way to access the value
	// in case of cache miss:
	override suspend fun directRequest(ref: Ref<Score>): out {
		// Due to a limitation of our API, we must cast the reference
		ensureValid(ref is Score.Ref) { "Found an unexpected reference type: $ref" }

		client.get<Score>("http://localhost:8080/${ref.id}")

		// There is no need to notify the cache here, because this function
		// is called by the cache in case of a cache miss, it will update 
		// itself using the returned value.
	}
}
```

This example was a bit simplified as it doesn't use DTOs, but I believe it does show that the Backbone library creates very little code overhead compared to a traditional approach: essentially the cache configuration, as well as notifying the cache of the side effects of the various methods.

Because we configured the cache to expire values automatically after some time, forgetting to expire the cache in some function is not a major issue.
It may cause users to see outdated values until the expiration timeout ends.
Simple unit tests will catch these mistakes.

In the above example, we used an HTTP client, but it could have been anything.
In practice, I like to take advantage of Kotlin Multiplatform by:

- declaring the objects in a common module,
- creating an in-memory fake implementation that I can write quickly in parallel of writing unit tests for the interface,
- creating the server-side implementation, that queries the database, using the tests written for the fake implementation to validate it,
- creating the client-side implementation, calling a fake server which responds using the fake implementation, again using the tests written for the fake to validate it.

This approach allows to:

- only write a single fake (since it's the same interface client-side and server-side),
- work on the UI using the fake implementation before the client and server implementations are written,
- work on the API using a fake as a repository before the persistence layer is implemented,
- write unit tests once for the fake, the client and the server, since they all conform to the same interface,
- unit test each implementation by always using the fake to replace other services used,
- because the Pedestal Cache library is used on all platforms, it is trivial to configure a server-side cache that caches over the database, and a client-side cache that caches over the HTTP requests, dramatically reducing network traffic and average latency.

Some schools of thought prefer mocking to faking, but you will still see that having a single interface client- and server-side helps with testing.

### 3. Reactive UIs

When writing reactive UIs, we often want to separate concerns over multiple components.
Ideally, each component would just know the ID of the object it needs to display, and would manage requesting new values completely by itself… in practice, however, doing this would mean each component in a single page would start their own dereference request!

Thanks to the aggressive caching in place with Backbone, this pattern has very little downsides.
Here is an example with a Compose-inspired syntax (but since all the magic is implemented with [Flow][kotlinx.coroutines.flow.Flow], it can easily be put in place with any reactive framework):

```kotlin
@Composable
fun ListScores(scores: Score.Service) {
	val mine by remember(scores) { scores.listMine().collectAsState(null) }

	for (score in mine ?: emptyList()) {
		Score(score)
	}
}

@Composable
fun Score(score: Score.Ref) {
	val value by remember { score.request().collectAsState(ProgressiveOutcome.Empty()) }

	value.onSuccess {
		Text("Current score: ${it.value}")
	}

	value.onFailure {
		Text("Couldn't access the score: $it", color = Color.Red)
	}

	value.onLoading {
		LoadingIndicator(it)
	}

	Button(onClick = { score.increase() }) {
		Text("Increase")
	}
}
```

The important things to notice in this snippet are:

- The UI subscribes to a value using `Score.Ref.request()`,
- `Score.Ref.request()` can be called multiple times per screen, or in multiple screens, and it will only start a single request,
- When calling `score.increase()`, we do not have to do anything to update the various UI components in the entirety of our application. They will automatically reflect the newest value, even if they are in a different screen.
