package opensavvy.state

import arrow.core.raise.ExperimentalTraceApi
import arrow.core.raise.ensure
import arrow.core.raise.ensureNotNull
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import opensavvy.prepared.compat.arrow.core.assertRaises
import opensavvy.prepared.compat.arrow.core.failOnRaise
import opensavvy.prepared.runner.kotest.PreparedSpec
import opensavvy.prepared.suite.prepared
import opensavvy.state.Counter.Service.Failures.*
import opensavvy.state.arrow.out
import opensavvy.state.outcome.Outcome
import opensavvy.state.outcome.failed
import kotlin.collections.List
import kotlin.jvm.JvmInline
import kotlin.random.Random

private data class User(
	// In real life, this would be a data class+Id+Service
	val name: String,
)

private data class Auth(
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

		suspend fun create(auth: Auth) = out<Create, Id> {
			ensureNotNull(auth.user) { Unauthenticated }

			val newId = Id(Random.nextInt())

			lock.withLock("create() by ${auth.user}") {
				data[newId] = Counter(auth.user, 0, emptySet())
			}

			newId
		}

		suspend fun list(auth: Auth) = out<Failures.List, List<Id>> {
			ensureNotNull(auth.user) { Unauthenticated }

			lock.withLock("list() by ${auth.user}") {
				val user: User = auth.user

				data
					.asSequence()
					.filter { (_, counter) -> counter.readableBy(user) }
					.map { it.key }
					.toList()
			}
		}

		suspend fun get(auth: Auth, id: Id) = out {
			ensureNotNull(auth.user) { Unauthenticated }

			val counter = lock.withLock("get($id) by ${auth.user}") { data[id] }
			ensureNotNull(counter) { NotFound(id) }
			ensure(counter.readableBy(auth.user)) { NotFound(id) } // Do not tell the user why they cannot see it

			counter
		}

		suspend fun increment(auth: Auth, id: Id) = out<Edit, Unit> {
			val counter = get(auth, id).bind()

			ensure(auth.user == counter.owner) { NotTheOwner(id) }

			// Possible data race here, but it's an imaginary example, so it's not a big deal
			lock.withLock("increment($id) by ${auth.user}") {
				data[id] = counter.copy(value = counter.value + 1)
			}
		}

		// Imagine there is also a 'decrement' method

		suspend fun share(auth: Auth, id: Id, user: User) = out<Share, Unit> {
			val counter = get(auth, id).bind()

			ensure(auth.user == counter.owner) { NotTheOwner(id) }

			// Possible data race here, but it's an imaginary example, so it's not a big deal
			lock.withLock("share($id, $user) by ${auth.user}") {
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

@OptIn(ExperimentalTraceApi::class)
class FailureEndToEndTest : PreparedSpec({

	val guest: User? = null
	val user1 = User("User 1")
	val user2 = User("User 2")

	val guestAuth = Auth(guest)
	val user1Auth = Auth(user1)
	val user2Auth = Auth(user2)

	val service by prepared { Counter.Service() }

	// Make test easier
	fun <T> Outcome<*, T>.getOrThrow() = when (this) {
		is Outcome.Failure -> error(failure.toString())
		is Outcome.Success -> value
	}

	suite("Guests") {
		test("Guests cannot create a counter") {
			service().create(guestAuth) shouldBe Unauthenticated.failed()
		}

		test("Guests cannot list counters") {
			service().list(guestAuth) shouldBe Unauthenticated.failed()
		}

		test("Guests cannot get counters") {
			val id = service().create(user1Auth).getOrThrow()

			service().get(guestAuth, id) shouldBe Unauthenticated.failed()
		}

		test("Guests cannot increment counters") {
			val id = service().create(user1Auth).getOrThrow()

			service().increment(guestAuth, id) shouldBe Unauthenticated.failed()
		}
	}

	suite("Users") {
		test("Users can create counters") {
			failOnRaise {
				out { service().create(user1Auth).bind() }
			}
		}

		test("Users can list their own counters") {
			failOnRaise {
				out {
					val id = service().create(user1Auth).bind()
					val results = service().list(user1Auth).bind()

					results shouldContain id
				}
			}
		}

		test("Users cannot list counters not shared to them") {
			failOnRaise {
				out {
					val id = service().create(user1Auth).bind()

					val results = service().list(user2Auth).bind()
					results shouldNotContain id
				}
			}
		}

		test("Users can get their own counters") {
			failOnRaise {
				out {
					val id = service().create(user1Auth).bind()

					service().get(user1Auth, id).bind() shouldBe Counter(user1, 0, emptySet())
				}
			}
		}

		test("Users cannot get counters not shared to them") {
			val id = failOnRaise {
				out {
					service().create(user1Auth).bind()
				}
			}

			assertRaises(NotFound(id) as Counter.Service.Failures) {
				out {
					service().get(user2Auth, id).bind()
				}
			}
		}

		test("Users cannot get a counter that doesn't exist") {
			val id = Counter.Id(2)

			assertRaises(NotFound(id) as Counter.Service.Failures) {
				out {
					service().get(user2Auth, id).bind()
				}
			}
		}

		test("Users can increment their own counters") {
			failOnRaise {
				out {
					val id = service().create(user1Auth).bind()
					service().increment(user1Auth, id).bind()

					service().get(user1Auth, id).bind() shouldBe Counter(user1, 1, emptySet())
				}
			}
		}

		test("Users can share their own counters") {
			failOnRaise {
				out {
					val id = service().create(user1Auth).bind()
					service().share(user1Auth, id, user2).bind()

					service().get(user1Auth, id).bind() shouldBe Counter(user1, 0, setOf(user2))
				}
			}
		}

		test("Users can list counters shared with them") {
			failOnRaise {
				out {
					val id = service().create(user1Auth).bind()
					service().share(user1Auth, id, user2).bind()

					service().list(user2Auth).bind() shouldContain id
				}
			}
		}

		test("Users can get counters shared with them") {
			failOnRaise {
				out {
					val id = service().create(user1Auth).bind()
					service().share(user1Auth, id, user2).bind()

					service().get(user2Auth, id).bind() shouldBe Counter(user1, 0, setOf(user2))
				}
			}
		}

		test("Users cannot increment shared counters") {
			failOnRaise {
				out {
					val id = service().create(user1Auth).bind()
					service().share(user1Auth, id, user2).bind()

					assertRaises(NotTheOwner(id) as Counter.Service.Failures) {
						out {
							service().increment(user2Auth, id).bind()
						}
					}
				}
			}
		}

		test("Users cannot share counters shared to them") {
			failOnRaise {
				out {
					val id = service().create(user1Auth).bind()
					service().share(user1Auth, id, user2).bind()

					assertRaises(NotTheOwner(id) as Counter.Service.Failures) {
						out {
							service().share(user2Auth, id, user1).bind()
						}
					}
				}
			}
		}
	}
})
