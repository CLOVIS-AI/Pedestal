package opensavvy.cache

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TestClock(private val scheduler: TestCoroutineScheduler) : Clock {
	override fun now(): Instant =
		Instant.fromEpochMilliseconds(scheduler.currentTime)
}

@OptIn(ExperimentalCoroutinesApi::class)
val TestScope.testClock get() = TestClock(testScheduler)
