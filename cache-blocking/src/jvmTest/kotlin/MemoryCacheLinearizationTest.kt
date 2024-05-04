package opensavvy.cache.blocking

import arrow.core.raise.ensure
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import opensavvy.cache.cache
import opensavvy.cache.cachedInMemory
import opensavvy.state.arrow.out
import org.jetbrains.kotlinx.lincheck.annotations.Operation
import org.jetbrains.kotlinx.lincheck.check
import org.jetbrains.kotlinx.lincheck.strategy.managed.modelchecking.ModelCheckingOptions
import org.jetbrains.kotlinx.lincheck.strategy.stress.StressOptions
import kotlin.random.Random
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class MemoryCacheLinearizationTest {

	private lateinit var job: Job
	private lateinit var cache: BlockingCache<Int, NegativeIntegerFailure, String>

	@BeforeTest
	fun createJob() {
		job = Job()

		/**
		 * The in-memory cache implementation is our system-under-test.
		 *
		 * In this example:
		 * - We are converting integers to strings.
		 * - Converting negative integers is forbidden, and raises a [NegativeIntegerFailure].
		 * - The in-memory cache stores items indefinitely.
		 */
		cache = cache<Int, NegativeIntegerFailure, String> {
			out {
				ensure(it >= 0) { NegativeIntegerFailure(it) }
				it.toString()
			}
		}.cachedInMemory(job)
			.blocking()
	}

	@AfterTest
	fun cancelJob() {
		job.cancel("The test is over")
	}

	data class NegativeIntegerFailure(val value: Int)

	@Operation
	fun get(i: Int) = cache[i]

	@Operation
	fun update(i: Int) {
		cache[i] = "$i"
	}

	@Operation
	fun simulateOutOfDate(i: Int) {
		cache[i] = "out of date ${Random.nextInt()}"
	}

	@Operation
	fun expire(i: Int) {
		cache.expire(i)
	}

	@Operation
	fun expireAll() {
		cache.expireAll()
	}

	@Test
	fun stressTest() {
		StressOptions().check(this::class)
	}

	@Test
	fun modelCheckingTesting() {
		ModelCheckingOptions().check(this::class)
	}
}
