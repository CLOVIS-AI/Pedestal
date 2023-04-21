# Module backbone

Lightweight pattern to integrate reactive caching in all layers of a multiplatform application.

## The problem

The core of modern applications is often sending data between machines.
However, most applications do this inefficiently:

- **Data is sent recursively**, sending large objects which contain sub-objects the client likely already knows (
  example: sending a list of comments under a post, which all repeat the author information as a small number of users
  are talking together).

- **Caching is difficult**, because it is not taken into account at the design stage; the app doesn't know which methods
  may invalidate the values.
  As a consequence, it is common to create pessimistic codebases, in which most functions query the newest value of the
  object, in case something else has changed it, wasting database performance.
  Another common approach is to make all object modifications server-side in the service layer, which makes server
  replication impossible.

- **Sharing code between the client and server is difficult**: what should the server do? What should the client do? It
  is common to duplicate the service layer between platforms.

- **Integration with reactive UI frameworks is difficult**: an object has no way to tell the framework when a new value
  is available.

Pedestal Backbone is an opinionated way of building APIs that attempts to solve these problems.
It builds upon Pedestal Progress, Pedestal State and Pedestal Cache to integrate reactive caching in all levels of an
application.
These projects are kept as independent building blocks to allow you to use them even if you do not adopt the Backbone
pattern.

## Existing patterns

Traditionally, code reuse has been based on inheritance.
However, distributed applications (such as client–server applications) must share data between an I/O boundary, making
inheritance impossible.

To integrate correctly with reactive frameworks, and to limit bugs in concurrent environments, it is recommended to work
with immutable data—contradicting the OOP model in which objects mutate themselves.

The functional programming paradigm proposes a solution: represent the values as simple immutable record types, and have
an accompanying class which manages mutations by returning new values (often called the Service layer). This approach
has been growing in popularity, for example in Hexagonal Architecture or Clean Architecture.

However, having a class manage mutation has limitations:

- code sharing between client and server is difficult, as the class should do different things,
- concrete values tend to be passed throughout the codebase, which makes it hard to know which value is the most recent,
  and thus makes integration with reactive frameworks difficult,
- working with heterogeneous implementations requires using a different pattern (e.g. a calendar app in which some
  events come from an in-house implementation and others come from third-party services using a different protocol).

## The Backbone pattern

The Backbone pattern represents domain objects as a triple: the domain object itself, a reference, and a manager.

The domain object itself is represented as an immutable final class (most often a regular `class`, a `data class` or
a `value class`, but `enum class` and `sealed class` are also possible). The domain object is responsible for data
validation (via its constructor·s) and computed properties.

Instead of directly using the domain object, most of the code should pass the reference between functions instead. When
code needs to access the value, it can request it via the reference's [request][opensavvy.backbone.Ref.request]
function. Values are cached by the manager, ensuring two subsequent reads with no write are cheap. The rest of the
codebase is thus free of wondering whether the value has been modified or not.

The manager is responsible for implementing the different actions, and managing the cache and its invalidation. The rest
of the codebase thus interacts with the manager to access references to new objects, and then passes those references
around, accessing their value when necessary.

Thanks to the cache, all functions except [request][opensavvy.backbone.Ref.request] should deal with references (either
as a parameter, or a return value). This ensures modifications do not depend on the external state and do not need a
prior read operation, and ensures all read operations are appropriately by the cache. Using this pattern, all read
operations are non-suspending and return a [ProgressiveFlow][opensavvy.state.coroutines.ProgressiveFlow]. All write
operations suspend and return a non-flow value (most often `Unit`, but they could also return a list of affected
references, or any other value).

Note that we purposefully do not call the manager a "Service": depending on your use case, you may use this pattern to
implement your Repository pattern, or any other class that manages changes to another class.

### In multiplatform applications

In a multiplatform application, the domain object is placed in the common module. The reference and manager are
interfaces declared in the common module, which respectively implement [Ref][opensavvy.backbone.Ref]
and [Backbone][opensavvy.backbone.Backbone].

Each implementation implements the reference and the manager (e.g. a client-side implementation which calls the API, and
a server-side implementation which calls the database). Each implementation benefits from the benefits of the cache, and
exposes the same API, making development easy (the service layer has the same signature client-side and server-side).

### Testing with fakes and spies

Because the manager is an interface and not a concrete class, it is easy to create an in-memory implementation for use
in tests. It is also easy to create an implementation that logs every call while delegating to another implementation.

These [test doubles](https://martinfowler.com/bliki/TestDouble.html) are respectively called fakes and spies. Together,
they offer the same power as mocks, and are much easier to use and debug as they are regular code.

## Example

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
	// We personally prefer declaring the service as a nested interface 
	// (so it as addressed as Score.Service) instead of its own top-level 
	// interface (e.g. ScoreService), but this is entirely up to you.

	// The service implements the Backbone interface.
	interface Service : Backbone<Ref, Failures, Score> {

		// Methods should:
		// - only accept/return references, and not actual values
        //   (this is necessary to ensure the cache catches all requests,
        //   the 'Ref.request' method should be the only one which returns
        //   a real object).
		// - return an Outcome or ProgressiveOutcome instance for error management
		//   (see the documentation of Pedestal State).
		//   ProgressiveFlow is used for cacheable read operations.
		suspend fun increment(score: Ref, amount: Int = 1): Outcome<Failures, Unit>

		// Using the same rules, we see that search operations
		// return references instead of returning the value directly.
		fun listMine(): ProgressiveFlow<Failures, Ref>
	}

	// We can now declare references to a specific score.
	// Mutability is expressed as a different value being returned upon
	// dereference operations over time.
	// Again, it is not mandatory to place the reference as a nested class.

	// The reference should store enough information for the service
	// implementations to find which object is referenced.
	// This could mean anything you want.
	interface Ref : BackboneRef<Failures, Score> {

		// Because we will always use references in our entire application,
		// it can be convenient to expose shorthands to execute operations.
		suspend fun increment(amount: Int = 1): Outcome<Failures, Unit>
	}
    
	// Following the best practice from Pedestal State,
	// we expose a sealed class of the various failure cases.
	// This avoids hardcoding the textual representation of errors,
	// making internationalization and error recovery.    
	sealed class Failures : opensavvy.state.failure.Failure {
		// Declare your failure cases as data classes or objects
	}
}
```

All that is left to do is to implement the `Score.Service` interface in our various modules (HTTP API, repository
layer…).
Notice how the amount of code written is very similar to the amount of code necessary for a traditional approach,
however,
as we will see in the next sections, this pattern is much more powerful.

### 2. Implementations and testing

Here is a sample client-side implementation of the interface, using a Ktor-inspired HTTP client. This example uses the
optional `arrow-state` dependency to profit from Arrow's typed error DSL.

```kotlin
import sun.jvm.hotspot.oops.CellTypeState.refimport java.sql.Refimport kotlin.coroutines.CoroutineScope

class ClientScoreRef(
        internal val id: String,
        private val backbone: ClientScores,
) : Score.Ref {

	// Provide convenience functions to act on this reference.
	override fun request() = backbone.request(this)
	override fun increment(amount: Int = 1) = backbone.increment(this, amount)
}

class ClientScores(
	private val client: HttpClient,
	cacheScope: CoroutineScope,
) : Score.Service {

	// First, we must select our caching strategy.
	// For more information, see the Pedestal Cache documentation.
	// Here, we cache the values in RAM for a maximum of 15 minutes
	// (after which the values are either re-requested if they are
	// still needed, or forgotten otherwise).
	private val cache = cache<ClientScoreRef, Failures, Score> {
		out {
			// This is an imaginary HTTP client.
			// Of course, this could be any library you like.
			client.get<Score>("http://localhost:8080/${ref.id}")
		}        
	}
		.cachedInMemory(cacheScope.job)
            .expireAfter(15.minutes, cacheScope)

  fun request(ref: ClientScoreRef) = cache[ref]

  override suspend fun increment(score: ClientScoreRef, amount: Int) = out {         
		client.post("http://localhost:8080/${score.id}?amount=$amount")

		// We know the score was just modified, we thus clean the cache.
		// It will decide by itself whether it's better to re-query the value
		// or just to delete it.
		// It will also automatically notify all UI components that
		// display this score.
		cache.expire(score)

		// If we were in a situation where the server returned the updated
		// value, we could instead inform the cache directly:
		// cache.update(score, newValue)

		// Notice that this function did NOT need to know what the current
		// value of the score is. In a traditional application where values
		// are passed to functions instead of references, a previous READ
		// operation would have been necessary to call this function.
	}

	override fun listMine() = out {
		client.get<List<Int>>("http://localhost:8080/myScores")
			// convert the IDs to references linked to this 
			// backbone implementation
			.map { ClientScoreRef(it, this@ClientScores) }

		// Here, no value is modified, so we do not need to inform the 
		// cache of anything.

		// If this endpoint returned full values instead of just their IDs,
		// we could call update here to avoid future dereference
		// requests.
	}.withProgress()
		.let { flowOf(it) } // Satisfy the super interface without caching.
		// If we later decide that caching would be beneficial, it is easy to add.
}
```

This example was a bit simplified as it doesn't use DTOs, but we believe it does show that the Backbone library creates
very little code overhead compared to a traditional approach: essentially the cache configuration, as well as notifying
the cache of the side effects of the various methods.

Because we configured the cache to expire values automatically after some time, forgetting to expire the cache in some
function is not a major issue.
It may cause users to see outdated values until the expiration timeout ends.
Simple unit tests will catch these mistakes.

In the above example, we used an HTTP client, but it could have been anything.
In practice, we like to take advantage of Kotlin Multiplatform by:

- declaring the objects in a common module,
- creating an in-memory fake implementation that can be written quickly in parallel of writing unit tests for the
  interface,
- creating the server-side implementation, that queries the database, using the tests written for the fake
  implementation to validate it,
- creating the client-side implementation, calling a fake server which responds using the fake implementation, again using the tests written for the fake to validate it.

This approach allows to:

- only write a single fake (since it's the same interface client-side and server-side),
- work on the UI using the fake implementation before the client and server implementations are written,
- work on the API using a fake as a repository before the persistence layer is implemented,
- write unit tests once for the fake, the client and the server, since they all conform to the same interface,
- test each implementation by using the fake to replace its dependencies,
- because the Pedestal Cache library is used on all platforms, it is trivial to configure a server-side cache that
  caches over the database, and a client-side cache that caches over the HTTP requests, dramatically reducing network
  traffic and average latency.

### 3. Reactive UIs

When writing reactive UIs, we often want to separate concerns over multiple components.
Ideally, each component would just know the ID of the object it needs to display, and would manage requesting new values
completely by itself… in practice, however, doing this would mean each component in a single page would start their own
dereference request.

Thanks to the aggressive caching Backbone encourages, only a single request will be started for the entire application.
Here is an example with a Compose-inspired syntax. The reactivity is implemented
using [Flow][kotlinx.coroutines.flow.Flow], and therefore works with any reactive framework).

```kotlin
@Composable
fun ListScores(scores: Score.Service) {
	val mine by remember(scores) { scores.listMine().collectAsState(null) }

	mine.onSuccess {
		for (score in it ?: emptyList()) {
			Score(score)
		}
	}
    
	mine.onFailure {
		Error("Couldn't access your scores: $it")
	}
    
	mine.onIncomplete {
		ListSkeleton()
	}
}

@Composable
fun Score(score: Score.Ref) {
	val value by remember { score.request().collectAsState(ProgressiveOutcome.Empty()) }

	value.onSuccess {
		Text("Current score: ${it.value}")
	}

	value.onFailure {
		Error("Couldn't access the score: $it")
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
- `Score.Ref.request()` can be called multiple times per screen, or in multiple screens, and it will only start a single
  request,
- When calling `score.increase()`, we do not have to do anything to update the various UI components in the entirety of
  our application. They will automatically reflect the newest value, even if they are in a different screen,
- We
  use [onSuccess][opensavvy.state.progressive.onSuccess], [onFailure][opensavvy.state.progressive.onFailure], [onLoading][opensavvy.state.progressive.onLoading]
  and [onIncomplete][opensavvy.state.progressive.onIncomplete] to manage the different possible results.
