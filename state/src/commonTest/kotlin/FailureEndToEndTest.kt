package opensavvy.state

import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import opensavvy.state.arrow.out
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import opensavvy.state.outcome.successful
import kotlin.jvm.JvmInline
import kotlin.random.Random
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class FailureEndToEndTest {

	private data class User(
		// In real life, this would be a data class+Id+Service
		val name: String,
	)

	private data class Context(
		val user: User?,
	)

	/**
	 * A counter has a [value].
	 *
	 * Only its [owner] can change the [value], and select the users the counter is [shared with][canRead].
	 */
	private data class Counter(
		val owner: User,
		val value: Int,
		val canRead: Set<User>,
	) {

		fun readableBy(user: User) = user == owner || user in canRead

		@JvmInline
		value class Id(val id: Int)

		// In real life, this would be an interface with multiple implementations
		class Service {
			private val lock = Mutex()
			private val data = HashMap<Id, Counter>()

			suspend fun create(context: Context) = out<Failures.Create, Id> {
				ensureNotNull(context.user) { Failures.Unauthenticated }

				val newId = Id(Random.nextInt())

				lock.withLock("create() by ${context.user}") {
					data[newId] = Counter(context.user, 0, emptySet())
				}

				newId
			}

			suspend fun list(context: Context) = out<Failures.List, List<Id>> {
				ensureNotNull(context.user) { Failures.Unauthenticated }

				lock.withLock("list() by ${context.user}") {
					val user: User = context.user

					data
						.asSequence()
						.filter { (_, counter) -> counter.readableBy(user) }
						.map { it.key }
						.toList()
				}
			}

			suspend fun get(context: Context, id: Id) = out {
				ensureNotNull(context.user) { Failures.Unauthenticated }

				val counter = lock.withLock("get($id) by ${context.user}") { data[id] }
				ensureNotNull(counter) { Failures.NotFound(id) }
				ensure(counter.readableBy(context.user)) { Failures.NotFound(id) } // Do not tell the user why they cannot see it

				counter
			}

			suspend fun increment(context: Context, id: Id) = out<Failures.Edit, Unit> {
				val counter = get(context, id).bind()

				ensure(context.user == counter.owner) { Failures.NotTheOwner(id) }

				// Possible data race here, but it's an imaginary example, so it's not a big deal
				lock.withLock("increment($id) by ${context.user}") {
					data[id] = counter.copy(value = counter.value + 1)
				}
			}

			// Imagine there is also a 'decrement' method

			suspend fun share(context: Context, id: Id, user: User) = out<Failures.Share, Unit> {
				val counter = get(context, id).bind()

				ensure(context.user == counter.owner) { Failures.NotTheOwner(id) }

				// Possible data race here, but it's an imaginary example, so it's not a big deal
				lock.withLock("share($id, $user) by ${context.user}") {
					data[id] = counter.copy(canRead = counter.canRead + user)
				}
			}

			// Imagine there is also a 'unshare' method

			// Now, we're listing all possible error cases of this service
			sealed interface Failures {

				// Declare the various operations this service can do

				sealed interface Create : Failures
				sealed interface List : Failures
				sealed interface Get : Failures, Edit, Share // <- Edit implies all failures of Get
				sealed interface Edit : Failures
				sealed interface Share : Failures

				// Declare the various errors and which operations they can be created by

				data class NotFound(val id: Id) : Failures,
					Get,
					Edit,
					Share {

					override fun toString() = "Could not find user with ID $id"
				}

				data class NotTheOwner(val id: Id) : Failures,
					Edit,
					Share {

					override fun toString() = "Only the owner can access $id"
				}

				object Unauthenticated : Failures,
					Create,
					List,
					Get,
					Edit,
					Share {

					override fun toString() = "Authentication is required to access counters"
				}
			}
		}
	}

	private val guest: User? = null
	private val user1 = User("User 1")
	private val user2 = User("User 2")

	private val guestContext = Context(guest)
	private val user1Context = Context(user1)
	private val user2Context = Context(user2)

	// Make test easier
	private fun <T> Outcome<*, T>.getOrThrow() = when (this) {
		is Outcome.Failure -> error(failure.toString())
		is Outcome.Success -> value
	}

	@Test
	fun guestsCannotCreateACounter() = runTest {
		val service = Counter.Service()

		assertEquals(
			Counter.Service.Failures.Unauthenticated.failed(),
			service.create(guestContext)
		)
	}

	@Test
	fun guestsCannotListCounters() = runTest {
		val service = Counter.Service()

		assertEquals(
			Counter.Service.Failures.Unauthenticated.failed(),
			service.list(guestContext)
		)
	}

	@Test
	fun guestsCannotGetCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()

		assertEquals(
			Counter.Service.Failures.Unauthenticated.failed(),
			service.get(guestContext, id),
		)
	}

	@Test
	fun guestsCannotIncrementCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()

		assertEquals(
			Counter.Service.Failures.Unauthenticated.failed(),
			service.increment(guestContext, id),
		)
	}

	@Test
	fun usersCanCreateCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context)
		assertIs<Outcome.Success<Counter.Id>>(id)
	}

	@Test
	fun usersListTheirOwnCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()

		val results = service.list(user1Context)
		assertIs<Outcome.Success<List<Counter.Id>>>(results)
		assertContains(results.value, id)
	}

	@Test
	fun usersCannotListUnsharedCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()

		val results = service.list(user2Context)
		assertIs<Outcome.Success<List<Counter.Id>>>(results)
		assertFalse(id in results.value)
	}

	@Test
	fun usersCanGetTheirOwnCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()

		assertEquals(
			Counter(user1, 0, emptySet()).successful(),
			service.get(user1Context, id),
		)
	}

	@Test
	fun usersCannotGetUnsharedCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()

		assertEquals(
			Counter.Service.Failures.NotFound(id).failed(),
			service.get(user2Context, id),
		)
	}

	@Test
	fun usersCannotGetACounterThatDoesntExist() = runTest {
		val service = Counter.Service()

		val id = Counter.Id(2)

		assertEquals(
			Counter.Service.Failures.NotFound(id).failed(),
			service.get(user2Context, id),
		)
	}

	@Test
	fun usersCanIncrementTheirOwnCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()

		val result = service.increment(user1Context, id)
		assertIs<Outcome.Success<*>>(result)

		assertEquals(
			Counter(user1, 1, emptySet()).successful(),
			service.get(user1Context, id),
		)
	}

	@Test
	fun usersCanShareTheirOwnCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()

		val result = service.share(user1Context, id, user2)
		assertIs<Outcome.Success<*>>(result)

		assertEquals(
			Counter(user1, 0, setOf(user2)).successful(),
			service.get(user1Context, id),
		)
	}

	@Test
	fun usersCanListCountersSharedWithThem() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()
		service.share(user1Context, id, user2).getOrThrow()

		val results = service.list(user2Context)
		assertIs<Outcome.Success<List<Counter.Id>>>(results)
		assertContains(results.value, id)
	}

	@Test
	fun usersCanGetCountersSharedWithThem() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()
		service.share(user1Context, id, user2).getOrThrow()

		assertEquals(
			Counter(user1, 0, setOf(user2)).successful(),
			service.get(user2Context, id),
		)
	}

	@Test
	fun usersCannotIncrementSharedCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()
		service.share(user1Context, id, user2).getOrThrow()

		assertEquals(
			Counter.Service.Failures.NotTheOwner(id).failed(),
			service.increment(user2Context, id),
		)
	}

	@Test
	fun usersCannotShareSharedCounters() = runTest {
		val service = Counter.Service()

		val id = service.create(user1Context).getOrThrow()
		service.share(user1Context, id, user2).getOrThrow()

		assertEquals(
			Counter.Service.Failures.NotTheOwner(id).failed(),
			service.share(user2Context, id, user1),
		)
	}
}
